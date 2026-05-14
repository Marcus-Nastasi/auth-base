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

# pem files
variable "rsa_private_pem" {
  description = "Conteúdo do private.pem"
  sensitive   = true
}

variable "rsa_public_pem" {
  description = "Conteúdo do public.pem"
  sensitive   = true
}
