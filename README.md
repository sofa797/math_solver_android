# Math Solver App
Android app for solving linear equations using server API

## How to run
create a `.env` file and insert your password from db in `your password`
bash```
DATABASE_URL=postgresql+psycopg2://postgres:your_password@db:5432/mathsolver
HF_API_KEY=your_hf_api_key
```
then
bash```
docker compose up --build
```
then open in your browser
bash```
http://localhost:8000
```
