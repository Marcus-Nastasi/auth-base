terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.6.0"
    }
  }

  # Guarda o estado do Terraform no S3 (evita conflitos em time)
  backend "s3" {
    bucket = "auth-base-terraform-state"   # crie este bucket manualmente antes
    key    = "output/terraform.tfstate"
    region = "sa-east-1"
  }
}

provider "aws" {
  region = var.aws_region
}
