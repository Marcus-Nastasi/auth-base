# Security group do RDS — só aceita conexão vinda do ECS
resource "aws_security_group" "rds" {
  name   = "${var.project_name}-rds-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port  = 5432
    to_port    = 5432
    protocol   = "tcp"
    #security_groups = []
  }
}

resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet"
  subnet_ids = aws_subnet.private[*].id
}

resource "aws_db_instance" "main" {
  identifier        = "${var.project_name}-db-${var.environment}"
  engine            = "postgres"
  engine_version    = "16"
  instance_class    = "db.t3.micro"
  allocated_storage = 5

  db_name                     = var.db_name
  manage_master_user_password = true
  username                    = var.db_username
  #password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [ aws_security_group.rds.id ]

  skip_final_snapshot         = true   # mudar para false em produção real
  multi_az                    = false  # mudar para true em produção real
}
