
module "code_pipeline" {
  source                      = "./modules/code_pipeline"
  repository_url_pytorch         = module.ecs.repository_url_pytorch
  repository_url_tensorflow         = module.ecs.repository_url_tensorflow
  region                      = var.region
  ecs_service_name_tensorflow        = module.ecs.service_name_tensorflow
  ecs_service_name_pytorch       = module.ecs.service_name_pytorch
  ecs_cluster_name            = module.ecs.cluster_name
  run_task_subnet_id          = module.net.private_subnets_id[0]
  run_task_security_group_ids = flatten([ module.net.security_groups_ids, module.ecs.security_group_id])
  MYSERVER		      = var.MYSERVER
  MYISERVER		      = var.MYISERVER
}
