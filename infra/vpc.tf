# Cria a rede isolada da aplicação
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true

  tags = {
    Env     = var.environment
    Project = var.project_name
    Name    = "${var.project_name}-vpc"
  }
}

# Subnets PÚBLICAS — ficam o ALB e o NAT Gateway
resource "aws_subnet" "public" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name    = "${var.project_name}-public-${count.index}"
    Env     = var.environment
    Project = var.project_name
  }
}

# Subnets PRIVADAS — ficam o ECS e o RDS (sem acesso direto da internet)
resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.${count.index + 10}.0/24"
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "${var.project_name}-private-${count.index}"
    Env     = var.environment
    Project = var.project_name
  }
}

# Internet Gateway — permite tráfego externo nas subnets públicas
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Env     = var.environment
    Project = var.project_name
  }}

# Route table para subnets públicas
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Env     = var.environment
    Project = var.project_name
  }
}

resource "aws_route_table_association" "public" {
  count          = 2
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id

  tags = {
    Env     = var.environment
    Project = var.project_name
  }
}
