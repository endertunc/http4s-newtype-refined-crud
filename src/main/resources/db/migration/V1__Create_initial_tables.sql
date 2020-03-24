CREATE TABLE car_advert (
  id VARCHAR(255) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  price INTEGER NOT NULL,
  offerType VARCHAR(10) NOT NULL,
  fuelType VARCHAR(10) NOT NULL,
  mileage INTEGER DEFAULT NULL,
  firstRegistration DATE DEFAULT NULL,
  CONSTRAINT car_advert_id_unique UNIQUE (id)
);
