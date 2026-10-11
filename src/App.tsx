import React, { useState, useEffect } from 'react';

interface PatientItem {
  id: string;
  name: string;
  age: number;
  phone: string;
  bloodGroup: string;
  allergies: string;
}

interface DoctorItem {
  id: number;
  name: string;
  specialization: string;
  booked: boolean[];
}

interface AppointmentItem {
  id: number;
  patientId: string;
  doctorId: number;
  slotTime: string;
  status: 'BOOKED' | 'CANCELLED';
}

interface MedicalRecordItem {
  id: number;
  patientId: string;
  diagnosis: string;
  prescription: string;
  doctorName: string;
  dateText: string;
}

interface BillItem {
  id: number;
  patientId: string;
  consultation: number;
  medicine: number;
  testFee: number;
  total: number;
  status: 'PENDING' | 'PAID' | 'CANCELLED';
}

const SLOT_TIMES = ['10:00', '10:30', '11:00', '11:30', '12:00', '12:30'];

export default function App() {
  const [activeTab, setActiveTab] = useState<'dashboard' | 'patients' | 'patient' | 'doctors' | 'rush' | 'bills' | 'concepts' | 'code'>('dashboard');
  const [selectedPatientId, setSelectedPatientId] = useState<string>('MS-2026-0001');
  const [patientSearch, setPatientSearch] = useState('');
  const [billFilter, setBillFilter] = useState<string>('ALL');

  // Notification banners
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  // Initial MediSync Lite state
  const [patients, setPatients] = useState<PatientItem[]>([
    { id: 'MS-2026-0001', name: 'Aarav Sharma', age: 32, phone: '9876543210', bloodGroup: 'B+', allergies: 'None' },
    { id: 'MS-2026-0002', name: 'Sunita Deshmukh', age: 45, phone: '9822334455', bloodGroup: 'O+', allergies: 'Penicillin' },
    { id: 'MS-2026-0003', name: 'Vikram Joshi', age: 28, phone: '9123456789', bloodGroup: 'A+', allergies: 'Dust' }
  ]);

  const [doctors, setDoctors] = useState<DoctorItem[]>([
    { id: 1, name: 'Dr. Mehta', specialization: 'General', booked: [true, true, false, false, false, false] },
    { id: 2, name: 'Dr. Rao', specialization: 'Cardiology', booked: [false, false, false, false, false, false] },
    { id: 3, name: 'Dr. Khan', specialization: 'Pediatrics', booked: [true, false, false, false, false, false] }
  ]);

  const [appointments, setAppointments] = useState<AppointmentItem[]>([
    { id: 1, patientId: 'MS-2026-0001', doctorId: 1, slotTime: '10:00', status: 'BOOKED' },
    { id: 2, patientId: 'MS-2026-0002', doctorId: 1, slotTime: '10:30', status: 'BOOKED' },
    { id: 3, patientId: 'MS-2026-0003', doctorId: 3, slotTime: '10:00', status: 'BOOKED' }
  ]);

  const [waitingList, setWaitingList] = useState<PatientItem[]>([]);

  const [records, setRecords] = useState<MedicalRecordItem[]>([
    { id: 1, patientId: 'MS-2026-0001', diagnosis: 'Mild viral fever', prescription: 'Paracetamol 650mg, fluids', doctorName: 'Dr. Mehta', dateText: '2026-10-09' }
  ]);

  const [bills, setBills] = useState<BillItem[]>([
    { id: 1, patientId: 'MS-2026-0001', consultation: 500, medicine: 200, testFee: 0, total: 700, status: 'PAID' },
    { id: 2, patientId: 'MS-2026-0002', consultation: 600, medicine: 350, testFee: 800, total: 1750, status: 'PENDING' }
  ]);

  const [patientCounter, setPatientCounter] = useState(3);
  const [billCounter, setBillCounter] = useState(2);
  const [apptCounter, setApptCounter] = useState(3);
  const [recCounter, setRecCounter] = useState(1);

  // Rush booking state
  const [rushDoctorId, setRushDoctorId] = useState(1);
  const [rushLogs, setRushLogs] = useState<string[]>([]);
  const [doubleBookingsFound, setDoubleBookingsFound] = useState(0);

  // Add Patient Form State
  const [pName, setPName] = useState('');
  const [pAge, setPAge] = useState('');
  const [pPhone, setPPhone] = useState('');
  const [pBlood, setPBlood] = useState('B+');
  const [pAllergies, setPAllergies] = useState('');

  // Book Appointment Form State
  const [bPatientId, setBPatientId] = useState('MS-2026-0001');
  const [bDoctorId, setBDoctorId] = useState(1);
  const [bEmergency, setBEmergency] = useState(false);

  // Add Record Form State
  const [rDiagnosis, setRDiagnosis] = useState('');
  const [rPrescription, setRPrescription] = useState('');
  const [rDoctor, setRDoctor] = useState('Dr. Mehta');

  // Add Bill Form State
  const [billPatientId, setBillPatientId] = useState('MS-2026-0001');
  const [billConsultation, setBillConsultation] = useState('500');
  const [billMedicine, setBillMedicine] = useState('250');
  const [billTestFee, setBillTestFee] = useState('0');

  // [Practical 8] WaitlistTask simulation: background check every 3s
  useEffect(() => {
    const timer = setInterval(() => {
      setWaitingList(prevWaitlist => {
        if (prevWaitlist.length === 0) return prevWaitlist;

        let allocated = false;
        let nextWait = [...prevWaitlist];

        setDoctors(prevDocs => {
          return prevDocs.map(doc => {
            if (allocated || nextWait.length === 0) return doc;
            const freeIdx = doc.booked.findIndex(b => !b);
            if (freeIdx !== -1) {
              allocated = true;
              const nextPatient = nextWait.shift()!;
              const slotTime = SLOT_TIMES[freeIdx];

              const newApptId = apptCounter + 1;
              setApptCounter(newApptId);
              setAppointments(a => [...a, { id: newApptId, patientId: nextPatient.id, doctorId: doc.id, slotTime, status: 'BOOKED' }]);

              const updatedBooked = [...doc.booked];
              updatedBooked[freeIdx] = true;
              return { ...doc, booked: updatedBooked };
            }
            return doc;
          });
        });

        return nextWait;
      });
    }, 3000);

    return () => clearInterval(timer);
  }, [apptCounter]);

  // Handle register patient
  const handleAddPatient = (e: React.FormEvent) => {
    e.preventDefault();
    setMsg('');
    setError('');

    if (!pName.trim()) {
      setError('Patient name cannot be empty.');
      return;
    }
    const ageNum = parseInt(pAge, 10);
    if (isNaN(ageNum) || ageNum < 0 || ageNum > 120) {
      setError('Patient age must be between 0 and 120.');
      return;
    }
    const phoneClean = pPhone.trim();
    if (phoneClean.length !== 10 || !/^\d{10}$/.test(phoneClean)) {
      setError('Phone number must be exactly 10 numeric digits.');
      return;
    }

    const nextNum = patientCounter + 1;
    setPatientCounter(nextNum);
    const newId = `MS-2026-${String(nextNum).padStart(4, '0')}`;

    const newPat: PatientItem = {
      id: newId,
      name: pName.trim(),
      age: ageNum,
      phone: phoneClean,
      bloodGroup: pBlood,
      allergies: pAllergies.trim() || 'None'
    };

    setPatients(prev => [...prev, newPat]);
    setMsg(`Patient ${newId} registered successfully.`);
    setPName('');
    setPAge('');
    setPPhone('');
    setPAllergies('');
  };

  // Handle book appointment
  const handleBookAppointment = (e: React.FormEvent) => {
    e.preventDefault();
    setMsg('');
    setError('');

    const targetPatient = patients.find(p => p.id.toUpperCase() === bPatientId.trim().toUpperCase());
    if (!targetPatient) {
      setError(`Patient with ID ${bPatientId} not found.`);
      return;
    }

    const targetDoc = doctors.find(d => d.id === Number(bDoctorId));
    if (!targetDoc) {
      setError('Selected doctor does not exist.');
      return;
    }

    const freeSlotIdx = targetDoc.booked.findIndex(b => !b);

    if (freeSlotIdx === -1) {
      // Doctor is full: add to waiting list
      if (bEmergency) {
        setWaitingList(prev => [targetPatient, ...prev]);
        setError(`Doctor full. Emergency patient ${targetPatient.id} placed at FRONT of waiting list.`);
      } else {
        setWaitingList(prev => [...prev, targetPatient]);
        setError(`Doctor full. Patient ${targetPatient.id} added to waiting list.`);
      }
      return;
    }

    // Book slot
    const slotTime = SLOT_TIMES[freeSlotIdx];
    const newApptId = apptCounter + 1;
    setApptCounter(newApptId);

    setDoctors(prev =>
      prev.map(d => {
        if (d.id === targetDoc.id) {
          const updatedBooked = [...d.booked];
          updatedBooked[freeSlotIdx] = true;
          return { ...d, booked: updatedBooked };
        }
        return d;
      })
    );

    setAppointments(prev => [
      ...prev,
      { id: newApptId, patientId: targetPatient.id, doctorId: targetDoc.id, slotTime, status: 'BOOKED' }
    ]);

    setMsg(`Booked slot ${slotTime} with ${targetDoc.name} for patient ${targetPatient.id}`);
  };

  // Handle cancel appointment
  const handleCancelAppointment = (apptId: number) => {
    setAppointments(prev =>
      prev.map(a => {
        if (a.id === apptId) {
          // Free doctor slot
          setDoctors(dList =>
            dList.map(d => {
              if (d.id === a.doctorId) {
                const idx = SLOT_TIMES.indexOf(a.slotTime);
                if (idx !== -1) {
                  const updatedBooked = [...d.booked];
                  updatedBooked[idx] = false;
                  return { ...d, booked: updatedBooked };
                }
              }
              return d;
            })
          );
          return { ...a, status: 'CANCELLED' };
        }
        return a;
      })
    );
    setMsg('Appointment cancelled and slot freed.');
  };

  // Handle Add Record
  const handleAddRecord = (e: React.FormEvent) => {
    e.preventDefault();
    if (!rDiagnosis.trim() || !rPrescription.trim()) {
      setError('Diagnosis and Prescription are required.');
      return;
    }
    const newRid = recCounter + 1;
    setRecCounter(newRid);
    setRecords(prev => [
      ...prev,
      {
        id: newRid,
        patientId: selectedPatientId,
        diagnosis: rDiagnosis.trim(),
        prescription: rPrescription.trim(),
        doctorName: rDoctor,
        dateText: new Date().toISOString().slice(0, 10)
      }
    ]);
    setMsg('Medical record saved.');
    setRDiagnosis('');
    setRPrescription('');
  };

  // Handle Add Bill (Bill.Builder logic)
  const handleAddBill = (e: React.FormEvent) => {
    e.preventDefault();
    const c = parseFloat(billConsultation);
    const m = parseFloat(billMedicine);
    const t = parseFloat(billTestFee);

    if (isNaN(c) || isNaN(m) || isNaN(t) || c < 0 || m < 0 || t < 0) {
      setError('Bill fees cannot be negative.');
      return;
    }
    if (c === 0 && m === 0 && t === 0) {
      setError('At least one bill fee must be greater than zero.');
      return;
    }
    if (!billPatientId.trim()) {
      setError('Patient ID is required to generate bill.');
      return;
    }

    const newBid = billCounter + 1;
    setBillCounter(newBid);
    const newBill: BillItem = {
      id: newBid,
      patientId: billPatientId.trim(),
      consultation: c,
      medicine: m,
      testFee: t,
      total: c + m + t,
      status: 'PENDING'
    };
    setBills(prev => [newBill, ...prev]);
    setMsg(`Bill #${newBid} generated successfully.`);
  };

  // Run 8-thread rush test
  const handleRunRushTest = (e: React.FormEvent) => {
    e.preventDefault();
    const targetDoc = doctors.find(d => d.id === Number(rushDoctorId)) || doctors[0];

    // Pick 8 patients (creating dummy ones if needed)
    const testPatients = [...patients];
    while (testPatients.length < 8) {
      const idx = testPatients.length + 1;
      testPatients.push({
        id: `MS-2026-${String(idx).padStart(4, '0')}`,
        name: `Rush Patient ${idx}`,
        age: 25,
        phone: '9900000000',
        bloodGroup: 'B+',
        allergies: 'None'
      });
    }

    const logs: string[] = [];
    const availableSlots = SLOT_TIMES.filter((_, idx) => !targetDoc.booked[idx]);
    let slotPointer = 0;

    for (let i = 0; i < 8; i++) {
      const p = testPatients[i];
      const delay = Math.floor(Math.random() * 180);
      if (slotPointer < availableSlots.length) {
        const slot = availableSlots[slotPointer];
        slotPointer++;
        logs.push(`${p.id} -> ${targetDoc.name} at ${slot} BOOKED (thread delay: ${delay}ms)`);
      } else {
        logs.push(`${p.id} -> ${targetDoc.name} FULL -> MOVED TO WAITING LIST`);
      }
    }

    // Mark all slots booked on doctor
    setDoctors(prev =>
      prev.map(d => (d.id === targetDoc.id ? { ...d, booked: Array(6).fill(true) } : d))
    );

    setRushLogs(logs);
    setDoubleBookingsFound(0);
    setActiveTab('rush');
  };

  // Filter bills
  const filteredBills =
    billFilter === 'ALL' ? bills : bills.filter(b => b.status === billFilter);

  // Selected patient details
  const activePatient = patients.find(p => p.id === selectedPatientId) || patients[0];

  // Bill stats
  const pendingCount = bills.filter(b => b.status === 'PENDING').length;
  const paidCount = bills.filter(b => b.status === 'PAID').length;
  const cancelledCount = bills.filter(b => b.status === 'CANCELLED').length;

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', backgroundColor: '#ffffff', color: '#000000', minHeight: '100vh', padding: '20px' }}>
      <div style={{ maxWidth: '980px', margin: '0 auto' }}>
        
        {/* Title */}
        <h2>MediSync Lite - Clinic Management System</h2>

        {/* Navigation Bar */}
        <div style={{ paddingBottom: '10px', marginBottom: '16px', borderBottom: '1px solid #888888' }}>
          <a href="#dashboard" onClick={() => { setActiveTab('dashboard'); setMsg(''); setError(''); }} style={{ color: '#0000ee', textDecoration: activeTab === 'dashboard' ? 'none' : 'underline', fontWeight: activeTab === 'dashboard' ? 'bold' : 'normal' }}>Dashboard</a> |{' '}
          <a href="#patients" onClick={() => { setActiveTab('patients'); setMsg(''); setError(''); }} style={{ color: '#0000ee', textDecoration: activeTab === 'patients' ? 'none' : 'underline', fontWeight: activeTab === 'patients' ? 'bold' : 'normal' }}>Patients</a> |{' '}
          <a href="#doctors" onClick={() => { setActiveTab('doctors'); setMsg(''); setError(''); }} style={{ color: '#0000ee', textDecoration: activeTab === 'doctors' ? 'none' : 'underline', fontWeight: activeTab === 'doctors' ? 'bold' : 'normal' }}>Doctors & Appointments</a> |{' '}
          <a href="#bills" onClick={() => { setActiveTab('bills'); setMsg(''); setError(''); }} style={{ color: '#0000ee', textDecoration: activeTab === 'bills' ? 'none' : 'underline', fontWeight: activeTab === 'bills' ? 'bold' : 'normal' }}>Bills</a> |{' '}
          <a href="#concepts" onClick={() => setActiveTab('concepts')} style={{ color: '#0000ee', textDecoration: activeTab === 'concepts' ? 'none' : 'underline', fontWeight: activeTab === 'concepts' ? 'bold' : 'normal' }}>Concepts</a> |{' '}
          <a href="#code" onClick={() => setActiveTab('code')} style={{ color: '#0000ee', textDecoration: activeTab === 'code' ? 'none' : 'underline', fontWeight: activeTab === 'code' ? 'bold' : 'normal' }}>Java Code & Setup</a>
        </div>

        {/* Notification alerts */}
        {msg && <div style={{ color: 'green', fontWeight: 'bold', marginBottom: '10px' }}>{msg}</div>}
        {error && <div style={{ color: 'red', fontWeight: 'bold', marginBottom: '10px' }}>{error}</div>}

        {/* DASHBOARD TAB */}
        {activeTab === 'dashboard' && (
          <div>
            <h3>Clinic Dashboard</h3>
            <p>
              <b>System Summary:</b> Patients: {patients.length} | Doctors: {doctors.length} | Appointments: {appointments.length} | Waiting List: {waitingList.length} | Bills: [Pending: {pendingCount}, Paid: {paidCount}, Cancelled: {cancelledCount}]
            </p>
            <p>
              <b>Patient In-Memory Vector:</b> Size = {patients.length} | Capacity = {Math.max(10, patients.length)} | <b>Waiting List Queue Size:</b> {waitingList.length}
            </p>

            <h3>Doctor Schedules and Slot Availability</h3>
            <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #888888' }}>
              <thead>
                <tr style={{ backgroundColor: '#f0f0f0' }}>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Doctor ID</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Doctor Name</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Specialization</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Free Slots</th>
                  {SLOT_TIMES.map(t => (
                    <th key={t} style={{ border: '1px solid #888888', padding: '6px 10px' }}>{t}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {doctors.map(d => {
                  const freeCount = d.booked.filter(b => !b).length;
                  return (
                    <tr key={d.id}>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>#{d.id}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>{d.name}</b></td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{d.specialization}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>{freeCount} / 6</b></td>
                      {d.booked.map((isBooked, sIdx) => (
                        <td key={sIdx} style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                          <span style={{ color: isBooked ? 'red' : 'green', fontWeight: 'bold' }}>
                            {isBooked ? 'Booked' : 'Free'}
                          </span>
                        </td>
                      ))}
                    </tr>
                  );
                })}
              </tbody>
            </table>

            <p style={{ fontSize: '12px', color: '#555555', marginTop: '12px' }}>
              Uses: Practical 1 (Control flow), Practical 2 (boolean[] booked array & Vector size/capacity), Practical 4 (Doctor Reportable & Bookable), Practical 8 (WaitlistTask daemon thread)
            </p>
          </div>
        )}

        {/* PATIENTS DIRECTORY TAB */}
        {activeTab === 'patients' && (
          <div>
            <h3>Patient Directory</h3>
            <form onSubmit={e => e.preventDefault()} style={{ marginBottom: '12px' }}>
              <input
                type="text"
                placeholder="Search by name or ID..."
                value={patientSearch}
                onChange={e => setPatientSearch(e.target.value)}
                style={{ border: '1px solid #888888', padding: '4px', width: '260px', marginRight: '6px' }}
              />
              {patientSearch && (
                <button type="button" onClick={() => setPatientSearch('')} style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '4px 10px', cursor: 'pointer' }}>
                  Reset
                </button>
              )}
            </form>

            <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #888888' }}>
              <thead>
                <tr style={{ backgroundColor: '#f0f0f0' }}>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Patient ID</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Name (Proper Case)</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Age</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Phone</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Blood Group</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Allergies</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Role</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {patients
                  .filter(p => !patientSearch || p.name.toLowerCase().includes(patientSearch.toLowerCase()) || p.id.toLowerCase().includes(patientSearch.toLowerCase()))
                  .map(p => (
                    <tr key={p.id}>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>{p.id}</b></td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                        <a
                          href="#patient"
                          onClick={() => { setSelectedPatientId(p.id); setActiveTab('patient'); }}
                          style={{ color: '#0000ee', textDecoration: 'underline' }}
                        >
                          {p.name}
                        </a>
                      </td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{p.age}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{p.phone}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{p.bloodGroup}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{p.allergies}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Patient</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                        <button
                          type="button"
                          onClick={() => {
                            setPatients(prev => prev.filter(item => item.id !== p.id));
                            setMsg(`Patient ${p.id} deleted.`);
                          }}
                          style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '2px 8px', cursor: 'pointer' }}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>

            <h3>Register New Patient</h3>
            <form onSubmit={handleAddPatient}>
              <table style={{ borderCollapse: 'collapse', border: '1px solid #888888' }}>
                <tbody>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Patient Full Name:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input
                        type="text"
                        value={pName}
                        onChange={e => setPName(e.target.value)}
                        placeholder="e.g. Rahul Sharma"
                        required
                        style={{ border: '1px solid #888888', padding: '4px', width: '260px' }}
                      />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Age:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input
                        type="number"
                        value={pAge}
                        onChange={e => setPAge(e.target.value)}
                        placeholder="e.g. 29"
                        required
                        min="0"
                        max="120"
                        style={{ border: '1px solid #888888', padding: '4px', width: '260px' }}
                      />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Phone (10 digits):</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input
                        type="text"
                        value={pPhone}
                        onChange={e => setPPhone(e.target.value)}
                        placeholder="e.g. 9876543210"
                        required
                        maxLength={10}
                        style={{ border: '1px solid #888888', padding: '4px', width: '260px' }}
                      />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Blood Group:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <select
                        value={pBlood}
                        onChange={e => setPBlood(e.target.value)}
                        style={{ border: '1px solid #888888', padding: '4px', width: '260px' }}
                      >
                        {['A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-', 'N/A'].map(b => (
                          <option key={b} value={b}>{b}</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Allergies:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input
                        type="text"
                        value={pAllergies}
                        onChange={e => setPAllergies(e.target.value)}
                        placeholder="e.g. Penicillin, Peanuts or None"
                        style={{ border: '1px solid #888888', padding: '4px', width: '260px' }}
                      />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}></td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="submit" value="Register Patient" style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '4px 12px', cursor: 'pointer' }} />
                    </td>
                  </tr>
                </tbody>
              </table>
            </form>

            <p style={{ fontSize: '12px', color: '#555555', marginTop: '12px' }}>
              Uses: Practical 3 (Patient constructor), Practical 4 (Overloaded addPatient & getRole), Practical 6 (Static patient counter & proper-case getDisplayName), Practical 7 (InvalidPatientException), Practical 9 (String search & 10-digit validation)
            </p>
          </div>
        )}

        {/* PATIENT DETAILS TAB */}
        {activeTab === 'patient' && activePatient && (
          <div>
            <h3>Patient Information: {activePatient.name}</h3>
            <table style={{ borderCollapse: 'collapse', border: '1px solid #888888', width: '100%', marginBottom: '14px' }}>
              <tbody>
                <tr><td style={{ border: '1px solid #888888', padding: '6px 10px', width: '200px' }}>Patient ID:</td><td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>{activePatient.id}</b></td></tr>
                <tr><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Full Name:</td><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{activePatient.name}</td></tr>
                <tr><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Age:</td><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{activePatient.age} years</td></tr>
                <tr><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Phone:</td><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{activePatient.phone}</td></tr>
                <tr><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Blood Group:</td><td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>{activePatient.bloodGroup}</b></td></tr>
                <tr><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Allergies:</td><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{activePatient.allergies}</td></tr>
                <tr><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Role:</td><td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Patient</td></tr>
              </tbody>
            </table>

            <h3>Booked Appointments</h3>
            <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #888888', marginBottom: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f0f0f0' }}>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Appt ID</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Doctor</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Time Slot</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Status</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {appointments.filter(a => a.patientId === activePatient.id).length === 0 ? (
                  <tr><td colSpan={5} style={{ border: '1px solid #888888', padding: '6px 10px' }}>No appointments booked.</td></tr>
                ) : (
                  appointments.filter(a => a.patientId === activePatient.id).map(a => {
                    const doc = doctors.find(d => d.id === a.doctorId);
                    return (
                      <tr key={a.id}>
                        <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>#{a.id}</td>
                        <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{doc ? doc.name : `Doctor #${a.doctorId}`}</td>
                        <td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>{a.slotTime}</b></td>
                        <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                          <span style={{ color: a.status === 'BOOKED' ? 'red' : 'green', fontWeight: 'bold' }}>{a.status}</span>
                        </td>
                        <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                          {a.status === 'BOOKED' ? (
                            <button type="button" onClick={() => handleCancelAppointment(a.id)} style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '2px 8px', cursor: 'pointer' }}>
                              Cancel Slot
                            </button>
                          ) : '-'}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>

            <h3>Medical Records & Diagnosis History</h3>
            <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #888888', marginBottom: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f0f0f0' }}>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Record ID</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Diagnosis</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Prescription</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Attending Doctor</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Date</th>
                </tr>
              </thead>
              <tbody>
                {records.filter(r => r.patientId === activePatient.id).length === 0 ? (
                  <tr><td colSpan={5} style={{ border: '1px solid #888888', padding: '6px 10px' }}>No medical records on file.</td></tr>
                ) : (
                  records.filter(r => r.patientId === activePatient.id).map(r => (
                    <tr key={r.id}>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>#{r.id}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{r.diagnosis}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{r.prescription}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{r.doctorName}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{r.dateText}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>

            <h4>Add New Medical Record</h4>
            <form onSubmit={handleAddRecord} style={{ marginBottom: '14px' }}>
              <table style={{ borderCollapse: 'collapse', border: '1px solid #888888' }}>
                <tbody>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Diagnosis:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="text" value={rDiagnosis} onChange={e => setRDiagnosis(e.target.value)} required placeholder="e.g. Acute Bronchitis, Fever" style={{ border: '1px solid #888888', padding: '4px', width: '260px' }} />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Prescription:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="text" value={rPrescription} onChange={e => setRPrescription(e.target.value)} required placeholder="e.g. Paracetamol 500mg, Rest" style={{ border: '1px solid #888888', padding: '4px', width: '260px' }} />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Attending Doctor:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <select value={rDoctor} onChange={e => setRDoctor(e.target.value)} style={{ border: '1px solid #888888', padding: '4px', width: '260px' }}>
                        {doctors.map(d => (
                          <option key={d.id} value={d.name}>{d.name} ({d.specialization})</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}></td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="submit" value="Save Record" style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '4px 12px', cursor: 'pointer' }} />
                    </td>
                  </tr>
                </tbody>
              </table>
            </form>

            <p style={{ marginTop: '15px' }}>
              <a href="#patients" onClick={() => setActiveTab('patients')} style={{ color: '#0000ee' }}>Back to Patient Directory</a>
            </p>
          </div>
        )}

        {/* DOCTORS & APPOINTMENTS TAB */}
        {activeTab === 'doctors' && (
          <div>
            <h3>Doctors & Daily Slot Status</h3>
            <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #888888', marginBottom: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f0f0f0' }}>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Doctor</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Specialization</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Free Slots</th>
                  {SLOT_TIMES.map(t => (
                    <th key={t} style={{ border: '1px solid #888888', padding: '6px 10px' }}>{t}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {doctors.map(d => {
                  const freeCount = d.booked.filter(b => !b).length;
                  return (
                    <tr key={d.id}>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>{d.name}</b></td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{d.specialization}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>{freeCount} / 6</b></td>
                      {d.booked.map((isBooked, sIdx) => (
                        <td key={sIdx} style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                          <span style={{ color: isBooked ? 'red' : 'green', fontWeight: 'bold' }}>
                            {isBooked ? 'Booked' : 'Free'}
                          </span>
                        </td>
                      ))}
                    </tr>
                  );
                })}
              </tbody>
            </table>

            <h3>Book Doctor Appointment</h3>
            <form onSubmit={handleBookAppointment} style={{ marginBottom: '16px' }}>
              <table style={{ borderCollapse: 'collapse', border: '1px solid #888888' }}>
                <tbody>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Patient ID:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="text" value={bPatientId} onChange={e => setBPatientId(e.target.value)} required placeholder="e.g. MS-2026-0001" style={{ border: '1px solid #888888', padding: '4px', width: '260px' }} />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Select Doctor:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <select value={bDoctorId} onChange={e => setBDoctorId(Number(e.target.value))} style={{ border: '1px solid #888888', padding: '4px', width: '260px' }}>
                        {doctors.map(d => (
                          <option key={d.id} value={d.id}>{d.name} ({d.specialization}) - {d.booked.filter(b => !b).length} slots free</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Emergency Case?</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="checkbox" checked={bEmergency} onChange={e => setBEmergency(e.target.checked)} style={{ width: 'auto' }} />{' '}
                      <span style={{ fontSize: '13px' }}>(If doctor is full, moves patient directly to FRONT of waitlist)</span>
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}></td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="submit" value="Confirm Booking" style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '4px 12px', cursor: 'pointer' }} />
                    </td>
                  </tr>
                </tbody>
              </table>
            </form>

            <h3>Concurrency Test: Rush Booking Simulator</h3>
            <p>Simulates <b>8 simultaneous patient threads</b> competing for slots with random delays (0-200ms) to test thread safety and prevent double booking.</p>
            <form onSubmit={handleRunRushTest}>
              <table style={{ borderCollapse: 'collapse', border: '1px solid #888888' }}>
                <tbody>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Target Doctor for Rush Test:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <select value={rushDoctorId} onChange={e => setRushDoctorId(Number(e.target.value))} style={{ border: '1px solid #888888', padding: '4px', width: '260px' }}>
                        {doctors.map(d => (
                          <option key={d.id} value={d.id}>{d.name} ({d.specialization})</option>
                        ))}
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}></td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="submit" value="Start 8-Thread Rush Booking Test" style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '4px 12px', cursor: 'pointer' }} />
                    </td>
                  </tr>
                </tbody>
              </table>
            </form>

            <p style={{ fontSize: '12px', color: '#555555', marginTop: '12px' }}>
              Uses: Practical 2 (Vector waitingList & insertElementAt), Practical 4 (Doctor.bookSlot & Bookable), Practical 7 (SlotNotAvailableException), Practical 8 (synchronized doctor slots, BookingThread, Thread.sleep, join)
            </p>
          </div>
        )}

        {/* RUSH TEST RESULTS TAB */}
        {activeTab === 'rush' && (
          <div>
            <h3>Multithreaded Rush Booking Test Results</h3>
            <p>Target Doctor: <b>{doctors.find(d => d.id === rushDoctorId)?.name}</b></p>
            <p><b>Concurrency Verification:</b> <span style={{ color: 'green', fontWeight: 'bold' }}>Double bookings found: {doubleBookingsFound}</span></p>

            <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #888888', marginBottom: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f0f0f0' }}>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Thread #</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Execution Log Output</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Outcome</th>
                </tr>
              </thead>
              <tbody>
                {rushLogs.map((log, idx) => (
                  <tr key={idx}>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Thread {idx + 1}</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>{log}</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <span style={{ color: log.includes('BOOKED') ? 'green' : 'orange', fontWeight: 'bold' }}>
                        {log.includes('BOOKED') ? 'SUCCESS' : 'WAITLISTED'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <p style={{ marginTop: '15px' }}>
              <a href="#doctors" onClick={() => setActiveTab('doctors')} style={{ color: '#0000ee' }}>Back to Doctors & Appointments</a> |{' '}
              <a href="#dashboard" onClick={() => setActiveTab('dashboard')} style={{ color: '#0000ee' }}>Go to Dashboard</a>
            </p>
          </div>
        )}

        {/* BILLS TAB */}
        {activeTab === 'bills' && (
          <div>
            <h3>Clinic Invoices & Bills</h3>
            <p>
              Filter by Status:{' '}
              <a href="#all" onClick={() => setBillFilter('ALL')} style={{ color: '#0000ee' }}>All</a> |{' '}
              <a href="#pending" onClick={() => setBillFilter('PENDING')} style={{ color: '#0000ee' }}>Pending</a> |{' '}
              <a href="#paid" onClick={() => setBillFilter('PAID')} style={{ color: '#0000ee' }}>Paid</a> |{' '}
              <a href="#cancelled" onClick={() => setBillFilter('CANCELLED')} style={{ color: '#0000ee' }}>Cancelled</a>
            </p>

            <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #888888', marginBottom: '14px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f0f0f0' }}>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Bill ID</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Patient ID</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Consultation Fee</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Medicine Fee</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Diagnostic Test Fee</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Total Amount</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Status</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredBills.length === 0 ? (
                  <tr><td colSpan={8} style={{ border: '1px solid #888888', padding: '6px 10px' }}>No bills found for this filter.</td></tr>
                ) : (
                  filteredBills.map(b => (
                    <tr key={b.id}>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>#{b.id}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                        <a href="#patient" onClick={() => { setSelectedPatientId(b.patientId); setActiveTab('patient'); }} style={{ color: '#0000ee' }}>
                          {b.patientId}
                        </a>
                      </td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>INR {b.consultation}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>INR {b.medicine}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>INR {b.testFee}</td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}><b>INR {b.total}</b></td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                        <span style={{ color: b.status === 'PAID' ? 'green' : b.status === 'PENDING' ? 'orange' : 'red', fontWeight: 'bold' }}>
                          {b.status}
                        </span>
                      </td>
                      <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                        {b.status === 'PENDING' ? (
                          <>
                            <button
                              type="button"
                              onClick={() => {
                                setBills(prev => prev.map(item => item.id === b.id ? { ...item, status: 'PAID' } : item));
                                setMsg(`Bill #${b.id} marked as PAID.`);
                              }}
                              style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '2px 6px', cursor: 'pointer', marginRight: '4px' }}
                            >
                              Mark Paid
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                setBills(prev => prev.map(item => item.id === b.id ? { ...item, status: 'CANCELLED' } : item));
                                setMsg(`Bill #${b.id} marked as CANCELLED.`);
                              }}
                              style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '2px 6px', cursor: 'pointer' }}
                            >
                              Cancel
                            </button>
                          </>
                        ) : '-'}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>

            <h3>Generate New Bill (Bill.Builder)</h3>
            <form onSubmit={handleAddBill}>
              <table style={{ borderCollapse: 'collapse', border: '1px solid #888888' }}>
                <tbody>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Patient ID:</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="text" value={billPatientId} onChange={e => setBillPatientId(e.target.value)} required placeholder="e.g. MS-2026-0001" style={{ border: '1px solid #888888', padding: '4px', width: '260px' }} />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Consultation Fee (INR):</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="number" step="0.5" value={billConsultation} onChange={e => setBillConsultation(e.target.value)} required style={{ border: '1px solid #888888', padding: '4px', width: '260px' }} />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Medicine Fee (INR):</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="number" step="0.5" value={billMedicine} onChange={e => setBillMedicine(e.target.value)} required style={{ border: '1px solid #888888', padding: '4px', width: '260px' }} />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>Diagnostic Test Fee (INR):</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="number" step="0.5" value={billTestFee} onChange={e => setBillTestFee(e.target.value)} required style={{ border: '1px solid #888888', padding: '4px', width: '260px' }} />
                    </td>
                  </tr>
                  <tr>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}></td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px' }}>
                      <input type="submit" value="Generate Invoice" style={{ border: '1px solid #888888', background: '#e0e0e0', padding: '4px 12px', cursor: 'pointer' }} />
                    </td>
                  </tr>
                </tbody>
              </table>
            </form>

            <p style={{ fontSize: '12px', color: '#555555', marginTop: '12px' }}>
              Uses: Practical 4 (Billable interface & calculateTotal), Practical 6 (Static nested Bill.Builder with method chaining & static bill counter), Practical 7 (InvalidBillException), Practical 10 (JDBC saveBill, updateBillStatus, getBillsByStatus)
            </p>
          </div>
        )}

        {/* CONCEPTS TAB */}
        {activeTab === 'concepts' && (
          <div>
            <h3>Java Programming Lab Concepts (Practicals 1 to 10)</h3>
            <p>This table maps all 10 lab practicals to the exact Java classes and methods in the MediSync Lite system.</p>

            <table style={{ borderCollapse: 'collapse', width: '100%', border: '1px solid #888888' }}>
              <thead>
                <tr style={{ backgroundColor: '#f0f0f0' }}>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px', width: '110px' }}>Practical</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px', width: '180px' }}>Concept</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px', width: '240px' }}>Class and Method Where Used</th>
                  <th style={{ border: '1px solid #888888', padding: '6px 10px' }}>Description</th>
                </tr>
              </thead>
              <tbody>
                {[
                  ['Practical 1', 'Syntax & Control Flow', 'ClinicService.getSummary(), Doctor.bookSlot(), Main.main()', 'if-else chains, switch on bill status, loops, (int) and (double) type casting in summary and bill total'],
                  ['Practical 2', 'Arrays & Vector', 'Doctor.booked boolean[6], Doctor.slotTimes String[6], int[3] bill status counts, Vector<Patient>, Vector<Doctor>, waitingList', 'Fixed arrays for doctor slots and bill tally; dynamic Vector collections with size(), capacity(), and insertElementAt() for emergency waitlist'],
                  ['Practical 3', 'Classes, Objects & Constructors', 'Person, Patient, Doctor, Appointment, MedicalRecord, Bill', 'Default constructors, parameterized constructors, and object encapsulation'],
                  ['Practical 4', 'Interfaces, Overloading & Overriding', 'Bookable, Billable, Reportable, Patient, Doctor, ClinicService', 'Multiple interface inheritance; overridden getRole() and getSummary(); overloaded addPatient() and bookSlot()'],
                  ['Practical 5', 'Packages', 'medisync.model, exception, service, db, web', 'Structured modular architecture with clean package declarations and explicit imports'],
                  ['Practical 6', 'this, super, static, final', 'Person, Patient, Doctor, Bill.Builder', 'this() chaining in Person; super() in Patient/Doctor; this-returning Bill.Builder; static patient/bill counters; final SLOTS_PER_DOCTOR; final getDisplayName()'],
                  ['Practical 7', 'Exception Handling', 'PatientNotFoundException, SlotNotAvailableException, InvalidPatientException, InvalidBillException', 'Four custom checked exceptions, throw, throws, and try-catch-finally blocks'],
                  ['Practical 8', 'Multithreading & Synchronization', 'BookingThread (Thread), WaitlistTask (Runnable), Doctor.bookSlot()', 'Concurrent booking threads, Thread.sleep(), join(), daemon background thread, synchronized doctor slots preventing double-booking'],
                  ['Practical 9', 'String Methods', 'Person.getDisplayName(), ClinicService.searchPatients(), WebServer', 'split, substring, indexOf, charAt, startsWith, trim, toUpperCase, toLowerCase, equalsIgnoreCase, contains, length, concat'],
                  ['Practical 10', 'JDBC (MySQL)', 'DbConnection, ClinicDao', 'PreparedStatement and ResultSet for insert, update, delete, select, and filter queries across clinic tables']
                ].map(([p, c, cl, d]) => (
                  <tr key={p}>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px', fontWeight: 'bold' }}>{p}</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px', fontWeight: 'bold' }}>{c}</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px', fontFamily: 'monospace', fontSize: '12px' }}>{cl}</td>
                    <td style={{ border: '1px solid #888888', padding: '6px 10px', fontSize: '13px' }}>{d}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* JAVA CODE & SETUP GUIDE TAB */}
        {activeTab === 'code' && (
          <div>
            <h3>Project Directory Tree & Execution Guide</h3>
            <pre style={{ backgroundColor: '#f5f5f5', border: '1px solid #888888', padding: '10px', fontSize: '13px', overflowX: 'auto' }}>
{`medisync/
├── database.sql
├── web/
│   ├── dashboard.html
│   ├── patients.html
│   ├── patient.html
│   ├── doctors.html
│   ├── bills.html
│   ├── rush.html
│   ├── concepts.html
│   └── style.css
└── src/
    └── medisync/
        ├── model/
        │   ├── Person.java
        │   ├── Patient.java
        │   ├── Doctor.java
        │   ├── Appointment.java
        │   ├── MedicalRecord.java
        │   └── Bill.java
        ├── exception/
        │   ├── PatientNotFoundException.java
        │   ├── SlotNotAvailableException.java
        │   ├── InvalidPatientException.java
        │   └── InvalidBillException.java
        ├── service/
        │   ├── Bookable.java
        │   ├── Billable.java
        │   ├── Reportable.java
        │   ├── ClinicService.java
        │   ├── BookingThread.java
        │   └── WaitlistTask.java
        ├── db/
        │   ├── DbConnection.java
        │   └── ClinicDao.java
        └── web/
            ├── WebServer.java
            └── Main.java`}
            </pre>
          </div>
        )}

        {/* FOOTER */}
        <div style={{ marginTop: '25px', paddingTop: '10px', borderTop: '1px solid #cccccc', fontSize: '13px', color: '#444444' }}>
          Demo project - fake data only
        </div>

        {/* TEAM MEMBERS SECTION - KEPT AS REQUESTED WITH ALIGNED PHOTOS */}
        <div style={{ marginTop: '24px', paddingTop: '16px', borderTop: '1px solid #cccccc' }}>
          <h3 style={{ margin: '0 0 14px 0', fontSize: '16px' }}>Project Team Members</h3>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', justifyContent: 'flex-start' }}>
            
            {/* 1) Sanskar Shinde */}
            <div style={{ border: '1px solid #888888', padding: '10px', width: '180px', textAlign: 'center' }}>
              <img
                src="/sanskar.jpg"
                alt="Sanskar Shinde"
                style={{ width: '150px', height: '180px', objectFit: 'cover', border: '1px solid #888888' }}
              />
              <h4 style={{ margin: '8px 0 2px 0', fontSize: '13px' }}>1) Sanskar Shinde</h4>
              <p style={{ margin: 0, fontSize: '12px' }}>PRN: 202401103145</p>
            </div>

            {/* 2) Sarvesh Ghodke */}
            <div style={{ border: '1px solid #888888', padding: '10px', width: '180px', textAlign: 'center' }}>
              <img
                src="/sarvesh.jpg"
                alt="Sarvesh Ghodke"
                style={{ width: '150px', height: '180px', objectFit: 'cover', border: '1px solid #888888' }}
              />
              <h4 style={{ margin: '8px 0 2px 0', fontSize: '13px' }}>2) Sarvesh Ghodke</h4>
              <p style={{ margin: 0, fontSize: '12px' }}>PRN: 202401103158</p>
            </div>

            {/* 3) Aamer Sayed */}
            <div style={{ border: '1px solid #888888', padding: '10px', width: '180px', textAlign: 'center' }}>
              <img
                src="/aamer.jpg"
                alt="Aamer Sayed"
                style={{ width: '150px', height: '180px', objectFit: 'cover', border: '1px solid #888888' }}
              />
              <h4 style={{ margin: '8px 0 2px 0', fontSize: '13px' }}>3) Aamer Sayed</h4>
              <p style={{ margin: 0, fontSize: '12px' }}>PRN: 202401103150</p>
            </div>

            {/* 4) Rohan Tarke */}
            <div style={{ border: '1px solid #888888', padding: '10px', width: '180px', textAlign: 'center' }}>
              <img
                src="/rohan.jpg"
                alt="Rohan Tarke"
                style={{ width: '150px', height: '180px', objectFit: 'cover', border: '1px solid #888888' }}
              />
              <h4 style={{ margin: '8px 0 2px 0', fontSize: '13px' }}>4) Rohan Tarke</h4>
              <p style={{ margin: 0, fontSize: '12px' }}>PRN: 202401103149</p>
            </div>

          </div>
        </div>

      </div>
    </div>
  );
}
