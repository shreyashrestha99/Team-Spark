import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, RequireRole } from './context/Auth'
import LoginPage from './Pages/Login'
import AdminDashboard from './Pages/AdminDashboard'
import StudentDashboard from './Pages/StudentDashboard'
import TeacherDashboard from './Pages/TeacherDashboard'
import './App.css'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LoginPage />} />
          <Route path="/login" element={<Navigate to="/" replace />} />
          <Route
            path="/admin/dashboard"
            element={
              <RequireRole roles={['ADMIN']}>
                <AdminDashboard />
              </RequireRole>
            }
          />
          <Route
            path="/student/dashboard"
            element={
              <RequireRole roles={['STUDENT']}>
                <StudentDashboard />
              </RequireRole>
            }
          />
          <Route
            path="/teacher/dashboard"
            element={
              <RequireRole roles={['TEACHER']}>
                <TeacherDashboard />
              </RequireRole>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
