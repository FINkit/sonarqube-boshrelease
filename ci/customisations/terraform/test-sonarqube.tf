resource "google_compute_firewall" "jumpbox-to-sonarqube" {
  name        = "${var.env_id}-jumpbox-to-sonarqube"
  network     = "${google_compute_network.bbl-network.name}"
  description = "Jumpbox to Sonarqube for test jobs"

  source_tags = ["${var.env_id}-jumpbox"]

  allow {
    ports    = ["9000"]
    protocol = "tcp"
  }

  target_tags = ["${var.env_id}-internal", "${var.env_id}-bosh-director"]
}
