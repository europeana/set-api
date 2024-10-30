package eu.europeana.api.set.integration.migration;

public class DepictionGenerationReport {
  int skipped = 0;
  int notGenerated = 0;
  int generated = 0;
  int getSkipped() {
    return skipped;
  }
  void setSkipped(int skipped) {
    this.skipped = skipped;
  }
  void increaseSkipped() {
    this.skipped++;
  }
  int getNotGenerated() {
    return notGenerated;
  }
  void setNotGenerated(int notGenerated) {
    this.notGenerated = notGenerated;
  }
  void increaseNotGenerated() {
    this.notGenerated++;
  }
  
  int getGenerated() {
    return generated;
  }
  void setGenerated(int generated) {
    this.generated = generated;
  }
  void increaseGenerated() {
    this.generated++;
  }
}
