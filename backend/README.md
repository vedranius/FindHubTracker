# FindHub Tracker Backend

Python backend server za praćenje Google Find Hub trackera.

## Instalacija

```bash
cd backend
python setup.py
```

## Konfiguracija

Uredi `.env` datoteku:

```env
HOST=0.0.0.0
PORT=8000
REFRESH_INTERVAL=300
TRACKER_FILTER=
```

## Pokretanje

```bash
python server.py
```

Server će biti dostupan na `http://localhost:8000`

## API Dokumentacija

Otvori `http://localhost:8000/docs` za interaktivnu dokumentaciju.

### Endpoints

| Method | Path | Opis |
|--------|------|------|
| GET | `/` | Status servera |
| GET | `/api/trackers` | Lista svih trackera |
| GET | `/api/trackers/{id}` | Detalji jednog trackera |
| POST | `/api/refresh` | Ručno osvježavanje lokacija |
| GET | `/api/config` | Konfiguracija |

## Povezivanje s Google Find Hub

Za povezivanje s pravim trackerima:

1. Instaliraj [GoogleFindMyTools](https://github.com/leonboe1/GoogleFindMyTools)
2. Pokreni `python main.py` za autentifikaciju
3. Kopiraj `Auth/secrets.json` u `backend/auth/`
4. Restartaj server

## Docker

```bash
docker build -t findhub-backend .
docker run -p 8000:8000 findhub-backend
```
