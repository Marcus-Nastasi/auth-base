# Security group do RDS — só aceita conexão vinda do ECS
resource "aws_security_group" "rds" {
  name   = "${var.project_name}-rds-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port  = 5432
    to_port    = 5432
    protocol   = var.db_ingress_protocol
    #security_groups = []
  }
}

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet"
  subnet_ids = aws_subnet.private[*].id
}

resource "aws_db_instance" "main" {
  identifier        = "${var.project_name}-db-${var.environment}"
  engine            = var.db_engine
  engine_version    = var.db_engine_version
  instance_class    = var.db_instance_class
  allocated_storage = var.db_allocated_storage
  kms_key_id        = aws_kms_key.kms_default_key.arn
  storage_encrypted = true

  db_name                     = var.db_name
  manage_master_user_password = var.db_manage_master_user_password
  username                    = var.db_username
  #password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [ aws_security_group.rds.id ]

  skip_final_snapshot = var.db_skip_final_snapshot   # mudar para false em produção real
  multi_az            = var.db_multi_az  # mudar para true em produção real
}
