const ADMIN_API = 'http://localhost:8080/api/admin'
const DOCS_API  = 'http://localhost:8080/api/documents'

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

export interface DocMeta {
  id: number
  fileName: string
  contentType: string
  uploadedAt: string
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

  async deleteUser(id: number): Promise<void> {
    await fetch(`${ADMIN_API}/users/${id}`, { method: 'DELETE' })
  },

  async getUserDocuments(userId: number): Promise<DocMeta[]> {
    const res = await fetch(`${DOCS_API}/user/${userId}`)
    return res.json()
  },

  getDocumentUrl(docId: number): string {
    return `${DOCS_API}/${docId}`
  },
}
