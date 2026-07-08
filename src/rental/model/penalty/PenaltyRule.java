package rental.model.penalty;

public interface PenaltyRule {
    double computeDamagePenalty(DamageSeverity severity);
}
