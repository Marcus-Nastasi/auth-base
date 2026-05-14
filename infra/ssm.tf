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

resource "aws_kms_key" "kms_default_key" {
  is_enabled = true
  enable_key_rotation = true
  deletion_window_in_days = 2
}
