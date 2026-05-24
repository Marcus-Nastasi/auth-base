resource "aws_elb" "main" {
  name = "${var.project_name}-elb-1"
  availability_zones = var.elb_availability_zones

  listener {
    instance_port     = 80
    instance_protocol = "http"
    lb_port           = 80
    lb_protocol       = "http"
  }
}
