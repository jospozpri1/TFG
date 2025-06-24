FROM python:3.10-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY app.py .

EXPOSE 5000
ENV PYTHONUNBUFFERED=1

CMD ["python","-u", "app.py"]
