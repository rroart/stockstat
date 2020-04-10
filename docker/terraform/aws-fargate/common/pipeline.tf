
module "code_pipeline" {
  source                      = "./modules/code_pipeline"
  region                      = var.region
  ecs_cluster_name            = module.ecs.cluster_name
  run_task_subnet_id          = module.net.private_subnets_id[0]
  run_task_security_group_ids = flatten([ module.net.security_groups_ids, module.ecs.security_group_id])
  MYSERVER		      = var.MYSERVER
}
