// General vars
variable "aws_region"  {
  type = string
  default = "sa-east-1"
}
variable "project_name" {
  type = string
  default = "auth-base"
}

variable "environment"  {
  type = string

  validation {
    condition = contains(["dev","hom","prod"], var.environment)
    error_message = "Environment var should  be dev, hom or prod"
  }
}

variable "app_port" {
  type = number
  default = 8080
}

# Database
variable "db_name" {
  type = string
  description = "Database's name"
  default = "postgres"
}
variable "db_username"  {
  type = string
  description = "Usuário do banco"
  default = "postgres"
}
variable "db_password"  {
  type = string
  description = "Senha do banco"
  default = ""
  sensitive = true
}
variable "db_ingress_protocol" {
  type    = string
  default = "tcp"
}
variable "db_engine" {
  type = string
  default = "postgres"
}
variable "db_engine_version" {
  type = string
  default = "16"
}
variable "db_instance_class" {
  type = string
  default = "db.t3.micro"
}
variable "db_allocated_storage" {
  type = number
  default = 5
  description = "storage in GB"
}
variable "db_manage_master_user_password" { type = bool }
variable "db_skip_final_snapshot" { type = bool }
variable "db_multi_az" { type = bool }

# pem files
variable "rsa_private_pem" {
  description = "Conteúdo do private.pem"
  sensitive   = true
}
variable "rsa_public_pem" {
  description = "Conteúdo do public.pem"
  sensitive   = true
}

// ELB
variable "elb_availability_zones" {
  type = list(string)
}
