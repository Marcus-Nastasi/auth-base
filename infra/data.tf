data "aws_region" "current" {}

data "aws_availability_zones" "available" {}

output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "rds_endpoint" {
  value     = aws_db_instance.main.address
  sensitive = true
}
