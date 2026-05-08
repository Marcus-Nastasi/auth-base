/**resource "aws_iam_role_policy" "ecs_ssm_access" {
  name = "${var.project_name}-ssm-access"
  role = aws_iam_role.ecs_task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = ["ssm:GetParameters", "ssm:GetParameter"]
      Resource = [
        "arn:aws:iam::*:root",
        "arn:aws:ssm:${var.aws_region}:*:parameter/${var.project_name}/*"
      ]
    }]
  })
}**/

# Mantém apenas as 3 imagens mais recentes (economiza custo)
resource "aws_ecr_lifecycle_policy" "app" {
  repository = aws_ecr_repository.app.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Manter ultimas 3 imagens"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 3
      }
      action = {
        type = "expire"
      }
    }]
  })
}
