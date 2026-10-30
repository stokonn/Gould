# Gould release ProGuard/R8 rules.
#
# Keep rules live here ONLY with a documented runtime reason; AAR consumer rules
# already protect Room, WorkManager, Compose and lifecycle entry points, and the
# application uses no reflection (serialization is platform org.json).
# Never add broad -keep rules for the whole package tree.
