# ZIO Http Ml server

This project shows how to integrate [ZIO Http library](https://github.com/dream11/zio-http)
(fast functional scala http server) with 
[MLeap](https://github.com/combust/mleap) which allows running ML models created using Scikit or Spark ML inside Scala applications.

Example contains one endpoint:
```http
POST /predict
```
that accepts simple json with one feature:
```json
{
  "f1": 0.23
}
```
and in response returns prediction:
```json
{
  "result": 0.8532893029776814
}
```

