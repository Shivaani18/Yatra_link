-- Create sequences
CREATE SEQUENCE booking_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE donation_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE occasion_seq START WITH 1 INCREMENT BY 1;

-- Booking table
CREATE TABLE temple_bookings (
    booking_id NUMBER DEFAULT booking_seq.NEXTVAL PRIMARY KEY,
    devotee_name VARCHAR2(100) NOT NULL,
    phone VARCHAR2(15) NOT NULL,
    visit_date DATE NOT NULL,
    visit_time VARCHAR2(20) NOT NULL,
    num_tickets NUMBER(2) NOT NULL,
    ticket_rate NUMBER(10,2) NOT NULL,
    total_amount NUMBER(10,2) NOT NULL,
    booking_time TIMESTAMP DEFAULT SYSTIMESTAMP,
    created_by VARCHAR2(50) DEFAULT USER
);

-- Donation table
CREATE TABLE temple_donations (
    donation_id NUMBER DEFAULT donation_seq.NEXTVAL PRIMARY KEY,
    donor_name VARCHAR2(100) NOT NULL,
    phone VARCHAR2(15),
    amount NUMBER(10,2) NOT NULL,
    purpose VARCHAR2(200),
    donation_date TIMESTAMP DEFAULT SYSTIMESTAMP,
    payment_mode VARCHAR2(20),
    created_by VARCHAR2(50) DEFAULT USER
);

-- Occasions table
CREATE TABLE temple_occasions (
    occasion_id NUMBER DEFAULT occasion_seq.NEXTVAL PRIMARY KEY,
    occasion_name VARCHAR2(100) NOT NULL,
    occasion_date DATE NOT NULL,
    description VARCHAR2(500),
    special_pooja VARCHAR2(100),
    ticket_rate NUMBER(10,2),
    created_by VARCHAR2(50) DEFAULT USER
);