output "app_url" {
  value = local.app_url
}

output "alb_dns_name" {
  value = aws_lb.main.dns_name
}

output "backend_ecr_url" {
  value = aws_ecr_repository.backend.repository_url
}

output "frontend_ecr_url" {
  value = aws_ecr_repository.frontend.repository_url
}

output "ecs_cluster" {
  value = aws_ecs_cluster.main.name
}

output "photos_bucket" {
  value = aws_s3_bucket.photos.bucket
}

output "db_endpoint" {
  value = aws_db_instance.main.address
}
