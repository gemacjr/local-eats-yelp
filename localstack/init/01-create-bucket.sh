#!/bin/bash
# Runs inside LocalStack once it is ready (mounted into /etc/localstack/init/ready.d).
set -euo pipefail
BUCKET="${S3_BUCKET:-yelp-photos}"

awslocal s3api create-bucket --bucket "$BUCKET" >/dev/null 2>&1 || true

# The browser PUTs photos straight to S3 with a presigned URL, so the bucket needs CORS.
awslocal s3api put-bucket-cors --bucket "$BUCKET" --cors-configuration '{
  "CORSRules": [{
    "AllowedOrigins": ["http://localhost:3000"],
    "AllowedMethods": ["PUT", "GET", "HEAD"],
    "AllowedHeaders": ["*"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }]
}'
echo "LocalStack: bucket '$BUCKET' ready with CORS"
