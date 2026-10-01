#!/usr/bin/env bash
# Build, push, and deploy the whole stack to AWS (ECS Fargate + RDS + S3 + ALB).
#
#   scripts/deploy-aws.sh            # deploy with tag = current git sha (or timestamp)
#   IMAGE_TAG=v1 scripts/deploy-aws.sh
#
# Requires: aws CLI v2 (authenticated), terraform >= 1.6, docker with buildx.
set -euo pipefail
cd "$(dirname "$0")/.."
TF=infra/terraform
TAG="${IMAGE_TAG:-$(git rev-parse --short HEAD 2>/dev/null || date +%Y%m%d%H%M%S)}"
REGION="${AWS_REGION:-$(grep -E '^\s*region' $TF/terraform.tfvars 2>/dev/null | sed -E 's/.*"(.*)"/\1/' || true)}"
REGION="${REGION:-us-east-1}"

echo "==> terraform init"
terraform -chdir=$TF init -input=false >/dev/null

# 1. ECR repos must exist before images can be pushed (and services need images to start).
echo "==> Creating ECR repositories"
terraform -chdir=$TF apply -input=false -auto-approve \
  -target=aws_ecr_repository.backend -target=aws_ecr_repository.frontend >/dev/null

BACKEND_REPO=$(terraform -chdir=$TF output -raw backend_ecr_url)
FRONTEND_REPO=$(terraform -chdir=$TF output -raw frontend_ecr_url)
REGISTRY="${BACKEND_REPO%%/*}"

echo "==> Logging in to $REGISTRY"
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$REGISTRY" >/dev/null

# 2. Build ARM64 images (Fargate tasks run on Graviton) and push.
echo "==> Building and pushing images (tag: $TAG)"
docker buildx build --platform linux/arm64 -t "$BACKEND_REPO:$TAG" --push backend
docker buildx build --platform linux/arm64 -t "$FRONTEND_REPO:$TAG" --push frontend

# 3. Everything else: VPC, RDS, S3, ALB, ECS services pointing at the new tag.
echo "==> Applying infrastructure"
terraform -chdir=$TF apply -input=false -auto-approve -var "image_tag=$TAG"

CLUSTER=$(terraform -chdir=$TF output -raw ecs_cluster)
echo "==> Waiting for ECS services to stabilize"
aws ecs wait services-stable --region "$REGION" --cluster "$CLUSTER" --services backend frontend

echo
echo "Deployed! $(terraform -chdir=$TF output -raw app_url)"
