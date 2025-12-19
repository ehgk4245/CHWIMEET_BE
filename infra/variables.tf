variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "Prefix for all resources"
  default     = "chwimeet"
}

variable "team_tag_value" { // 팀 태그 값 추가
  description = "Value for the 'Team' tag (e.g., devcos-team01)"
  default     = "chwimeet"
}

variable "app_1_domain" {
  description = "app_1 domain"
  default     = "api.chwimeet.dohakkk.site"
}

variable "key_name" {
  description = "EC2 Key Pair name"
  default     = "chwimeet_key"
}