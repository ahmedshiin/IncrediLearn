// Service API pour les appels backend
const API_URL = 'http://localhost:8080/api/auth'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  nom: string
  email: string
  password: string
  role: string
  domain?: string
}

export interface AuthResponse {
  id?: number
  nom?: string
  email?: string
  role?: string
  domain?: string
  isVerified?: boolean
  message: string
}

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await fetch(`${API_URL}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    })
    
    const data = await response.json()
    
    if (response.ok && data.id) {
      // Stocker les informations utilisateur dans localStorage
      localStorage.setItem('user', JSON.stringify(data))
    }
    
    return data
  },

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await fetch(`${API_URL}/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    })
    
    const data = await response.json()
    
    if (response.ok && data.id) {
      // Stocker les informations utilisateur dans localStorage
      localStorage.setItem('user', JSON.stringify(data))
    }
    
    return data
  },

  logout() {
    localStorage.removeItem('user')
  },

  getCurrentUser(): AuthResponse | null {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  },

  isAuthenticated(): boolean {
    return !!this.getCurrentUser()
  },
}
