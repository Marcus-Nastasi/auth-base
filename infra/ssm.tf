resource "aws_ssm_parameter" "rsa_private_pem" {
  name  = "/${var.project_name}/rsa_private_pem"
  type  = "SecureString"
  value = var.rsa_private_pem
}

resource "aws_ssm_parameter" "rsa_public_pem" {
  name  = "/${var.project_name}/rsa_public_pem"
  type  = "SecureString"
  value = var.rsa_public_pem
}
