# Stage 2 C4 — UI consolidation review

The route/back affordance now uses one `JournalBackButton` in editorial headers,
catalogue headers, and the Activity goal editor. It shares the accessible label
and RTL-aware Material icon while preserving each screen's surrounding layout.

The other candidate groups were reviewed against their rendered purpose:

- `Heading`, `SectionTitle`, `CompactSection`, and `EditorialSectionHeader`
  represent page introductions, plain section labels, compact inline actions,
  and editorial section headers. Their different hierarchy and action layout
  make a single component harder to use correctly.
- `WeeklyJournalChart` is a seven-day duration summary; Activity heatmap and
  year grid encode date-level activity and streak status. Their data and
  accessibility semantics differ.
- `MetricTile`, `SmallStatistic`, and Activity statistic cards have distinct
  sizes, interaction, and density requirements. A shared tile would either
  remove interaction or add configuration without removing meaningful code.
- Responsive thresholds remain local where based on available width, font
  scale, or content cardinality. No common breakpoint rule was evidenced.

Therefore the shared back affordance is the only consolidation made. The
remaining candidates stay separate to preserve behavior and avoid a generic
component with more branches than the existing components.
