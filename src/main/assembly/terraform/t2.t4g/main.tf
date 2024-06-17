variable "region" {
  description = "AWS region"
  type        = string
  default     = "eu-west-2"
}

variable "access_key" {
  description = "AWS Access Key"
  type        = string
}

variable "secret_key" {
  description = "AWS Secret Key"
  type        = string
}

variable "availability_zone" {
   description = "AWS Availability Zone"
   type        = string
   default     = "eu-west-2b"
}

variable "repo" {
  description = "Git repository for data storage"
  type        = string
}

provider "aws" {
  # Configuration options
  region = var.region
  access_key = var.access_key
  secret_key = var.secret_key
}


resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"  
  enable_dns_hostnames = true
  enable_dns_support = true
  tags = {
    Name = "VPC_PERFORMANCE_TEST"
  }
}

resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "VPC_PERFORMANCE_TEST_INTERNET_GATEWAY"
  }
}
# All traffic in the VPC will be routed
#    cidr_block = "10.0.1.0/24"
resource "aws_route_table" "main-route-table" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.gw.id
  }

  route {
    ipv6_cidr_block        = "::/0"
    gateway_id = aws_internet_gateway.gw.id
  }

  tags = {
    Name = "VPC_PERFORMANCE_TEST_ROUTE_TABLE"
  }
}

resource "aws_subnet" "main" {
  vpc_id     = aws_vpc.main.id
  cidr_block = "10.0.1.0/24"
  availability_zone = var.availability_zone

  tags = {
    Name = "VPC_PERFORMANCE_TEST_SUBNET"
  }
}

resource "aws_route_table_association" "a" {
  subnet_id      = aws_subnet.main.id
  route_table_id = aws_route_table.main-route-table.id
}

data "http" "local_ip" {
  url = "https://checkip.amazonaws.com/"
}


resource "aws_security_group" "allow_outbound_only" {
  name = "allow-outbound-only"
  description = "Allow outbound traffic only"
  vpc_id = aws_vpc.main.id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"  # All protocols
    cidr_blocks = ["0.0.0.0/0"]  # Allow outbound traffic to anywhere
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"  # All protocols
    cidr_blocks = ["${chomp(data.http.local_ip.response_body)}/32", aws_subnet.main.cidr_block]  # Only allow from my IP
  }

  tags = {
    Name = "VPC_PERFORMANCE_TEST_SUBNET"
  }
}

resource "aws_security_group" "internal_communication" {
  name = "internal-communication"
  description = "Allow all internal communication within the VPC"
  vpc_id = aws_vpc.main.id

  # Allow all inbound traffic from the VPC CIDR range
  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [aws_subnet.main.cidr_block]  # Replace with your VPC CIDR range
  }

  # Allow all outbound traffic to the VPC CIDR range
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [aws_subnet.main.cidr_block]  # Replace with your VPC CIDR range
  }
}

resource "aws_instance" "performance-test-client-t2-medium-server" {
  ami = "ami-0b9932f4918a00c4f"
  instance_type = "t2.medium"
#  instance_type = "t2.micro"
  availability_zone = var.availability_zone
  subnet_id = aws_subnet.main.id
  private_ip = "10.0.1.10"
  associate_public_ip_address = true       # Enable auto-assign public IP address
  key_name = "performance-test-key"
  security_groups = [aws_security_group.allow_outbound_only.id, aws_security_group.internal_communication.id]

  tags = {
    Name = "EU_WEST_2_EC2_MEDIUM_PT_CLIENT"
  }

  provisioner "file" {
    source      = "../../../../../target/performance-test-0.0.1-RELEASE-jar-with-dependencies.jar"
    destination = "performance-test-0.0.1-RELEASE-jar-with-dependencies.jar"
  }

  provisioner "file" {
    source      = "../../../../../target/performance-test-0.0.1-RELEASE.jar"
    destination = "performance-test-0.0.1-RELEASE.jar"
  }

# SSH private key for the git repo used to store the data.
  provisioner "file" {
    source      = "../../keys/independent-research-project.pem"
    destination = ".ssh/independent-research-project.pem"
  }

# SSH private key used for AWS EC2 deployments
  provisioner "file" {
    source      = "../../keys/performance-test-key.pem"
    destination = ".ssh/performance-test-key.pem"
  }

# Configuration file used to allow multiple key files to be assessed by default rather than just id_rsa
  provisioner "file" {
    source      = "../../keys/config"
    destination = ".ssh/config"
  }

  provisioner "file" {
    source      = "../../../resources/run_client.sh"
    destination = "run_client.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "while [ ! -f /usr/bin/java ]; do sleep 1; done",
      "while [ ! -f /usr/bin/git ]; do sleep 1; done",
      "chmod 400 ~/.ssh/id_rsa ~/.ssh/*.pem",
      "ssh-keyscan github.com >> ~/.ssh/known_hosts",
      "ssh-keyscan 10.0.1.11 >> ~/.ssh/known_hosts",
      "ssh-keyscan 10.0.1.12 >> ~/.ssh/known_hosts",
      "ssh-keyscan 10.0.1.13 >> ~/.ssh/known_hosts",
      "ssh-keyscan 10.0.1.14 >> ~/.ssh/known_hosts",
      "ssh-keyscan 10.0.1.15 >> ~/.ssh/known_hosts",
      "ssh-keyscan 10.0.1.16 >> ~/.ssh/known_hosts",
      "git clone ${var.repo}",
      "scp ~/performance-test-0.0.1-RELEASE.jar ubuntu@10.0.1.11:~/performance-test-0.0.1-RELEASE.jar",
      "scp ~/performance-test-0.0.1-RELEASE.jar ubuntu@10.0.1.12:~/performance-test-0.0.1-RELEASE.jar",
      "scp ~/performance-test-0.0.1-RELEASE.jar ubuntu@10.0.1.13:~/performance-test-0.0.1-RELEASE.jar",
      "scp ~/performance-test-0.0.1-RELEASE.jar ubuntu@10.0.1.14:~/performance-test-0.0.1-RELEASE.jar",
      "scp ~/performance-test-0.0.1-RELEASE.jar ubuntu@10.0.1.15:~/performance-test-0.0.1-RELEASE.jar",
      "scp ~/performance-test-0.0.1-RELEASE.jar ubuntu@10.0.1.16:~/performance-test-0.0.1-RELEASE.jar",
      "ssh ubuntu@10.0.1.11 bash /home/ubuntu/run_performance_test.sh 10.0.1.11 10000 8000m 24000m 0 1000000 1000000 60 100000 75000 t2.xlarge   > xlarge_amd_output.log &",
      "ssh ubuntu@10.0.1.12 bash /home/ubuntu/run_performance_test.sh 10.0.1.12 10000 8000m 24000m 0 1000000 1000000 60 100000 75000 t4g.xlarge  > xlarge_arm_output.log &",
      "ssh ubuntu@10.0.1.13 bash /home/ubuntu/run_performance_test.sh 10.0.1.13 10000 8000m 24000m 0 1000000 1000000 60 100000 75000 t2.large    > large_amd_output.log &",
      "ssh ubuntu@10.0.1.14 bash /home/ubuntu/run_performance_test.sh 10.0.1.14 10000 8000m 24000m 0 1000000 1000000 60 100000 75000 t4g.large   > large_arm_output.log &",
      "ssh ubuntu@10.0.1.15 bash /home/ubuntu/run_performance_test.sh 10.0.1.15 10000 8000m 24000m 0 1000000 1000000 60 100000 75000 t2.2xlarge  > 2xlarge_amd_output.log &",
      "ssh ubuntu@10.0.1.16 bash /home/ubuntu/run_performance_test.sh 10.0.1.16 10000 8000m 24000m 0 1000000 1000000 60 100000 75000 t4g.2xlarge > 2xlarge_arm_output.log &",
      "bash run_client.sh 10.0.1.11 10000 8080 10.0.1.12 10000 8080 10.0.1.13 10000 8080 10.0.1.14 10000 8080 10.0.1.15 10000 8080 10.0.1.16 10000 8080",
      "sudo shutdown -h"
    ]
  }

  connection {
    type        = "ssh"
    host        = self.public_ip
    user        = "ubuntu"
    private_key = file("../../keys/performance-test-key.pem")
    timeout     = "5m"
  }

  depends_on = [
    aws_instance.performance-test-amd-runner-t2-large-server,
    aws_instance.performance-test-arm-runner-t4g-large-server,
    aws_instance.performance-test-amd-runner-t2-xlarge-server,
    aws_instance.performance-test-arm-runner-t4g-xlarge-server,
    aws_instance.performance-test-amd-runner-t2-2xlarge-server,
    aws_instance.performance-test-arm-runner-t4g-2xlarge-server
  ]

  user_data = <<-EOF
#!/bin/bash
sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive TZ="Europe/London" apt-get -y install openjdk-21-jre git-all

sudo fallocate -l 1G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
EOF
}

resource "aws_instance" "performance-test-amd-runner-t2-large-server" {
#  instance_type = "t2.micro"
  ami = "ami-0b9932f4918a00c4f"
  instance_type = "t2.large"
  availability_zone = var.availability_zone
  subnet_id = aws_subnet.main.id
  private_ip = "10.0.1.13"
  associate_public_ip_address = true       # Enable auto-assign public IP address
  key_name = "performance-test-key"
  security_groups = [aws_security_group.allow_outbound_only.id, aws_security_group.internal_communication.id]

  tags = {
    Name = "EU_WEST_2_EC2_T2_LARGE_AMD_PT_RUNNER_1"
  }

  root_block_device {
      volume_size = 26
      volume_type = "gp2"
  }

  provisioner "file" {
    source      = "../../../resources/run_performance_test.sh"
    destination = "run_performance_test.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "ssh-keyscan 10.0.1.10 >> ~/.ssh/known_hosts",
      "while [ ! -f /usr/bin/java ]; do sleep 5; done"
    ]
  }

  connection {
    type        = "ssh"
    host        = self.public_ip
    user        = "ubuntu"
    private_key = file("../../keys/performance-test-key.pem")
    timeout     = "5m"
  }

  user_data = <<-EOF
#!/bin/bash
sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive TZ="Europe/London" apt-get -y install openjdk-21-jre

sudo fallocate -l 20G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
EOF
}

resource "aws_instance" "performance-test-arm-runner-t4g-large-server" {
#  ami = "ami-0b9932f4918a00c4f"
#  instance_type = "t2.micro"
  ami = "ami-01f09336b1295578b"
  instance_type = "t4g.large"
  availability_zone = var.availability_zone
  subnet_id = aws_subnet.main.id
  private_ip = "10.0.1.14"
  associate_public_ip_address = true       # Enable auto-assign public IP address
  key_name = "performance-test-key"
  security_groups = [aws_security_group.allow_outbound_only.id, aws_security_group.internal_communication.id]

  tags = {
    Name = "EU_WEST_2_EC2_T4G_LARGE_PT_ARM_RUNNER_1"
  }

  root_block_device {
      volume_size = 26
      volume_type = "gp2"
  }


  provisioner "file" {
    source      = "../../../resources/run_performance_test.sh"
    destination = "run_performance_test.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "ssh-keyscan 10.0.1.10 >> ~/.ssh/known_hosts",
      "while [ ! -f /usr/bin/java ]; do sleep 5; done"
    ]
  }

  connection {
    type        = "ssh"
    host        = self.public_ip
    user        = "ubuntu"
    private_key = file("../../keys/performance-test-key.pem")
    timeout     = "5m"
  }

  user_data = <<-EOF
#!/bin/bash
sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive TZ="Europe/London" apt-get -y install openjdk-21-jre

sudo fallocate -l 20G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
EOF
}

resource "aws_instance" "performance-test-amd-runner-t2-xlarge-server" {
  ami = "ami-0b9932f4918a00c4f"
#  instance_type = "t2.micro"
  instance_type = "t2.xlarge"
  availability_zone = var.availability_zone
  subnet_id = aws_subnet.main.id
  private_ip = "10.0.1.11"
  associate_public_ip_address = true       # Enable auto-assign public IP address
  key_name = "performance-test-key"
  security_groups = [aws_security_group.allow_outbound_only.id, aws_security_group.internal_communication.id]

  tags = {
    Name = "EU_WEST_2_EC2_T2_XLARGE_AMD_PT_RUNNER_1"
  }

  root_block_device {
      volume_size = 26
      volume_type = "gp2"
  }

  provisioner "file" {
    source      = "../../../resources/run_performance_test.sh"
    destination = "run_performance_test.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "ssh-keyscan 10.0.1.10 >> ~/.ssh/known_hosts",
      "while [ ! -f /usr/bin/java ]; do sleep 5; done"
    ]
  }

  connection {
    type        = "ssh"
    host        = self.public_ip
    user        = "ubuntu"
    private_key = file("../../keys/performance-test-key.pem")
    timeout     = "5m"
  }

  user_data = <<-EOF
#!/bin/bash
sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive TZ="Europe/London" apt-get -y install openjdk-21-jre

sudo fallocate -l 20G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
EOF
}

resource "aws_instance" "performance-test-arm-runner-t4g-xlarge-server" {
#  ami = "ami-0b9932f4918a00c4f"
#  instance_type = "t2.micro"
  ami = "ami-01f09336b1295578b"
  instance_type = "t4g.xlarge"
  availability_zone = var.availability_zone
  subnet_id = aws_subnet.main.id
  private_ip = "10.0.1.12"
  associate_public_ip_address = true       # Enable auto-assign public IP address
  key_name = "performance-test-key"
  security_groups = [aws_security_group.allow_outbound_only.id, aws_security_group.internal_communication.id]

  tags = {
    Name = "EU_WEST_2_EC2_T4G_XLARGE_PT_ARM_RUNNER_1"
  }

  root_block_device {
      volume_size = 26
      volume_type = "gp2"
  }


  provisioner "file" {
    source      = "../../../resources/run_performance_test.sh"
    destination = "run_performance_test.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "ssh-keyscan 10.0.1.10 >> ~/.ssh/known_hosts",
      "while [ ! -f /usr/bin/java ]; do sleep 5; done"
    ]
  }

  connection {
    type        = "ssh"
    host        = self.public_ip
    user        = "ubuntu"
    private_key = file("../../keys/performance-test-key.pem")
    timeout     = "5m"
  }

  user_data = <<-EOF
#!/bin/bash
sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive TZ="Europe/London" apt-get -y install openjdk-21-jre

sudo fallocate -l 20G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
EOF
}

resource "aws_instance" "performance-test-amd-runner-t2-2xlarge-server" {
  ami = "ami-0b9932f4918a00c4f"
#  instance_type = "t2.micro"
  instance_type = "t2.2xlarge"
  availability_zone = var.availability_zone
  subnet_id = aws_subnet.main.id
  private_ip = "10.0.1.15"
  associate_public_ip_address = true       # Enable auto-assign public IP address
  key_name = "performance-test-key"
  security_groups = [aws_security_group.allow_outbound_only.id, aws_security_group.internal_communication.id]

  tags = {
    Name = "EU_WEST_2_EC2_T2_2XLARGE_AMD_PT_RUNNER_1"
  }

  root_block_device {
      volume_size = 26
      volume_type = "gp2"
  }

  provisioner "file" {
    source      = "../../../resources/run_performance_test.sh"
    destination = "run_performance_test.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "ssh-keyscan 10.0.1.10 >> ~/.ssh/known_hosts",
      "while [ ! -f /usr/bin/java ]; do sleep 5; done"
    ]
  }

  connection {
    type        = "ssh"
    host        = self.public_ip
    user        = "ubuntu"
    private_key = file("../../keys/performance-test-key.pem")
    timeout     = "5m"
  }

  user_data = <<-EOF
#!/bin/bash
sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive TZ="Europe/London" apt-get -y install openjdk-21-jre

sudo fallocate -l 20G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
EOF
}

resource "aws_instance" "performance-test-arm-runner-t4g-2xlarge-server" {
#  ami = "ami-0b9932f4918a00c4f"
#  instance_type = "t2.micro"
  ami = "ami-01f09336b1295578b"
  instance_type = "t4g.2xlarge"
  availability_zone = var.availability_zone
  subnet_id = aws_subnet.main.id
  private_ip = "10.0.1.16"
  associate_public_ip_address = true       # Enable auto-assign public IP address
  key_name = "performance-test-key"
  security_groups = [aws_security_group.allow_outbound_only.id, aws_security_group.internal_communication.id]

  tags = {
    Name = "EU_WEST_2_EC2_T4G_2XLARGE_PT_ARM_RUNNER_1"
  }

  root_block_device {
      volume_size = 26
      volume_type = "gp2"
  }


  provisioner "file" {
    source      = "../../../resources/run_performance_test.sh"
    destination = "run_performance_test.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "ssh-keyscan 10.0.1.10 >> ~/.ssh/known_hosts",
      "while [ ! -f /usr/bin/java ]; do sleep 5; done"
    ]
  }

  connection {
    type        = "ssh"
    host        = self.public_ip
    user        = "ubuntu"
    private_key = file("../../keys/performance-test-key.pem")
    timeout     = "5m"
  }

  user_data = <<-EOF
#!/bin/bash
sudo apt-get update
sudo DEBIAN_FRONTEND=noninteractive TZ="Europe/London" apt-get -y install openjdk-21-jre

sudo fallocate -l 20G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
EOF
}
