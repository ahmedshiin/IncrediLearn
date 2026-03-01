const ADMIN_API = 'http://localhost:8080/api/admin'

export interface AdminStats {
  totalUsers: number
  activeCourses: number
  pendingValidations: number
  blockedUsers: number
}

export interface AdminUser {
  id: number
  nom: string
  email: string
  role: string
  domain?: string
  isVerified: boolean
  isBlocked: boolean
  createdAt: string
  courseCount: number
}

export const adminService = {
  async getStats(): Promise<AdminStats> {
    const res = await fetch(`${ADMIN_API}/stats`)
    return res.json()
  },

  async getUsers(): Promise<AdminUser[]> {
    const res = await fetch(`${ADMIN_API}/users`)
    return res.json()
  },

  async verifyUser(id: number, verified: boolean): Promise<AdminUser> {
    const res = await fetch(`${ADMIN_API}/users/${id}/verify`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ verified }),
    })
    return res.json()
  },

  async blockUser(id: number, blocked: boolean): Promise<AdminUser> {
    const res = await fetch(`${ADMIN_API}/users/${id}/block`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ blocked }),
    })
    return res.json()
  },
}
