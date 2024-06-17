rem These variables should be set globally for the system if testing with Docker too else they can be set here.
rem If the variables are left unset, data will be requested via prompts.
rem
rem Specify the AWS connection connection information
rem set TF_VAR_region=eu-west-2
rem set TF_VAR_availability_zone=eu-west-2b
rem set TF_VAR_secret_key=
rem set TF_VAR_access_key=
rem
rem Specify the git repo to store the results
rem set TF_VAR_repo=git@github.com:idk503cs/independent-research-project.git

terraform init -upgrade
terraform apply --auto-approve
terraform apply --destroy --auto-approve