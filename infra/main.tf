terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Guarda o estado do Terraform no S3 (evita conflitos em time)
  backend "s3" {
    bucket = "auth-base-terraform-state"   # crie este bucket manualmente antes
    key    = "${var.environment}/terraform.tfstate"
    region = var.aws_region
  }
}

provider "aws" {
  region = var.aws_region
}
