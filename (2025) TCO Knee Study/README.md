# TCO Knee Study

**By:** Jackson Sporer, Jackson Drahos, Hunter Dunn, Jon Roux

## Background Information

### Purpose
The purpose of our project is to find out how two different additional ACL reconstruction surgeries compare to:
- The intact knee state
- Each other
- The original ACLR surgery

This comparison was done across different trajectories and rotations of the knee to understand the distinct advantages and disadvantages of the two surgeries.

### Population/Sample
- **Population of interest:** Individuals with ACL tears looking for the best surgical treatment option.
- **Sample:** 20 knees from 10 cadavers.
  - All knees began in the **Intact** state.
  - An applied force moved the knees to the **Deficient** state (ACL torn).

### Data Collection
Data was obtained from the **TCO bioengineering department** using the following process:
1. All 20 knees start as **Intact**.
2. Force applied to simulate ACL tear → **Deficient**.
3. All knees receive the **initial ACLR surgery**.
4. Knees are split into two groups:
   - 10 receive **ALL surgery**
   - 10 receive **ML LET surgery**
5. Degrees of rotation were measured across 11 trajectories and 5 rotation types.

## Research Goals / Hypothesis
- Investigate how knee state affects rotation under different trajectories.
- Identify which surgery deviates least from the **Intact** state.
- Evaluate the pros/cons of **ALL** and **ML LET** surgeries.

**Hypothesis:** ACLR + ALL surgery will perform best overall.

## Model Selection
Initially planned to use **ANOVA**, but due to data formatting issues, we used **paied-sample t-tests** instead.

- **44 t-tests** run per rotation (5 rotations).
- **Bonferroni correction** applied: `alpha = 0.05 / 44 = 0.001136`.
- Significance tested against the **Intact** state (noted with ⁱ in tables).
- **Degrees of freedom:**
  - 9 for ACLR + ML LET and ACLR + ALL (n=10)
  - 19 for other states (n=20)

## Key Results: Internal Rotation (IR)

| Trajectory | Intact (n=20) | ACLR (n=20) | ACLR + ALL (n=10) | ACLR + ML LET (n=10) | ACL Deficient (n=20) |
|-----------|---------------|-------------|-------------------|----------------------|----------------------|
| ATT 30    | -2.0 ± 4.5    | 0.4 ± 4.4    | 1.1 ± 3.1         | -2.9 ± 5.8           | -1.6 ± 4.9           |
| ATT 90    | -1.6 ± 2.3    | -0.7 ± 2.8   | -1.6 ± 3.5        | -3.9 ± 5.0           | -2.2 ± 3.5           |
| IR 0      | 13.3 ± 5.5    | 2.3 ± 3.1    | 1.4 ± 2.5         | -0.5 ± 5.1           | 2.1 ± 3.0            |
| IR 15     | 10.9 ± 4.0    | 2.3 ± 3.4    | 1.6 ± 1.7         | -0.8 ± 5.1           | 1.9 ± 3.2            |
| IR 30     | 10.5 ± 3.2    | 2.2 ± 3.3    | 3.0 ± 2.0ⁱ        | -0.6 ± 5.1           | 1.8 ± 3.1            |
| IR 45     | 12.5 ± 4.0    | 2.0 ± 2.8    | 3.5 ± 2.9         | -0.4 ± 4.9           | 1.6 ± 2.7            |
| IR 60     | 8.7 ± 2.7     | 1.9 ± 2.6    | 3.4 ± 2.7         | -0.6 ± 4.9           | 1.5 ± 2.5            |
| IR 75     | 8.1 ± 2.4     | 1.8 ± 2.7    | 2.4 ± 2.9         | -1.1 ± 5.4           | 1.5 ± 2.7            |
| IR 90     | 8.1 ± 2.3     | 1.8 ± 3.0    | 1.3 ± 3.5         | -1.9 ± 6.3           | 1.5 ± 3.0            |
| Pivot 15  | 12.5 ± 5.8    | 2.9 ± 5.8    | 2.1 ± 1.9         | -0.1 ± 8.4           | 2.5 ± 5.7            |
| Pivot 30  | 11.8 ± 4.8    | 2.9 ± 6.3    | 3.1 ± 1.9ⁱ        | 0.2 ± 9.2            | 2.5 ± 6.2            |

ⁱ = Statistically significant difference from Intact (P < 0.00113)

### Interpretation
- **ACLR + ML LET** is most similar to the **Intact** knee (no significant difference).
- However, it **limits internal rotation**, as seen in negative values.
- Limitation may be undesirable if agility and full motion are desired.

## Model Limitations
- Small sample size (n=20) may reduce reliability.
- Data violated normality; outliers present.
- Applied **Benjamini-Hochberg** to control false discoveries.

## Future Research Ideas
- Investigate interaction effects between **state** and **trajectory**.
- Consider using **ANCOVA** to reduce error variance.
- Use **MANCOVA** to analyze across multiple rotations.

## Data Cleansing / Manipulation
- Converted raw data into differences from the Intact state.
- Fixed incorrect labels (e.g., “ACLR + LaPrede LET” → “ACLR + ALL”).
- Deleted duplicate rows and filled 2 missing values.

## Summary
- **ACLR + ML LET** is statistically closest to the intact knee, especially in Internal Rotation.
- It restricts movement, which may not suit all patients.
- A trade-off exists between structural similarity and joint mobility.

---

