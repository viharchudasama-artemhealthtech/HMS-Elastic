import csv
import random
from datetime import date, timedelta
import mysql.connector

DB = {
    "host": "localhost",
    "port": 1234,
    "user": "root",
    "password": "root123",
    "database": "hms_db_v3",
}

CATEGORIES = ["ANALGESICS", "ANTIBIOTICS", "ANTISEPTICS", "VITAMINS", "CARDIAC", "DIABETIC", "OTHER"]
MANUFACTURERS = ["Sun Pharma", "Cipla", "Dr. Reddy's", "Mankind", "Lupin", "Zydus"]

def random_expiry():
    return date.today() + timedelta(days=random.randint(180, 1800))

def code_for(i):
    return f"MED-{i:05d}"

def load_names(csv_file):
    names = []
    with open(csv_file, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)  # expects header ItemName
        for row in reader:
            n = row["ItemName"].strip().strip('"')
            if n:
                names.append(n)
    return names

def main():
    names = load_names("medicine_names.csv")

    conn = mysql.connector.connect(**DB)
    cur = conn.cursor()

    sql = """
    INSERT INTO medicines (
      created_at, created_by, updated_at, updated_by,
      category, description, dosage, expiry_date, is_active,
      manufacturer, medicine_code, name, quantity_in_stock, reorder_level, unit_price
    ) VALUES (
      NOW(), %s, NOW(), %s,
      %s, %s, %s, %s, %s,
      %s, %s, %s, %s, %s, %s
    )
    """

    data = []
    for i, name in enumerate(names, start=1):
        category = random.choice(CATEGORIES)
        manufacturer = random.choice(MANUFACTURERS)
        dosage = random.choice(["250mg", "500mg", "5ml", "10ml", "1g"])
        qty = random.randint(10, 500)
        reorder = random.randint(5, 50)
        price = round(random.uniform(5, 1500), 2)

        data.append((
            "admin", "admin",
            category,
            f"{category.title()} medicine",
            dosage,
            random_expiry(),
            1,
            manufacturer,
            code_for(i),
            name,
            qty,
            reorder,
            price
        ))

    cur.executemany(sql, data)
    conn.commit()
    print(f"Inserted {cur.rowcount} rows")

    cur.close()
    conn.close()

if __name__ == "__main__":
    main()