package rental.model.penalty;

public class DamagePenalty implements PenaltyRule {
    @Override
    public double computeDamagePenalty(DamageSeverity severity) {
        return switch (severity) {
            case LIGHT -> 10.0;
            case MODERATE -> 100.0;
            case HEAVY -> 1000.0;
            case NONE -> 0.0;
        };
    }

    public double fixedAmount(DamageSeverity severity) {
        return computeDamagePenalty(severity);
    }
}
