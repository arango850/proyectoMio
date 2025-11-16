MIO Graph Project

This small project reads the provided CSV files (`lines-241.csv`, `stops-241.csv`, `linestops-241.csv`) and builds the directed arc-graphs for each route and orientation, then prints ordered arcs per route.

Run with Gradle:

```
gradle run
```

Or with a specific Java executable:

```
gradle run --args='lines-241.csv stops-241.csv linestops-241.csv'
```

The program prints per route (line) the arcs in sequence for each orientation.
