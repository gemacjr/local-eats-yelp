variable "name" {
  description = "Name prefix for all resources"
  type        = string
  default     = "yelp"
}

variable "region" {
  type    = string
  default = "us-east-1"
}

variable "image_tag" {
  description = "Tag of the backend/frontend images in ECR to deploy"
  type        = string
  default     = "latest"
}

variable "certificate_arn" {
  description = "Optional ACM certificate ARN. When set, the ALB serves HTTPS and redirects HTTP."
  type        = string
  default     = ""
}

variable "app_domain" {
  description = "Optional public hostname (e.g. eats.example.com) pointing at the ALB. Defaults to the ALB DNS name."
  type        = string
  default     = ""
}

variable "db_instance_class" {
  type    = string
  default = "db.t4g.micro"
}

variable "db_multi_az" {
  description = "Multi-AZ RDS for production availability"
  type        = bool
  default     = false
}

variable "backend_desired_count" {
  type    = number
  default = 2
}

variable "frontend_desired_count" {
  type    = number
  default = 2
}

variable "single_nat_gateway" {
  description = "One shared NAT gateway (cheaper) instead of one per AZ (more available)"
  type        = bool
  default     = true
}
