import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { getCurrentUser } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { canAccessRouteLocation, getDefaultPath } from '@/utils/access'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录', hidden: true },
    },
    {
      path: '/',
      component: BasicLayout,
      redirect: '/student/applications',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'student/applications',
          name: 'student-applications',
          component: () => import('@/views/student/MyApplications.vue'),
          meta: {
            title: '我的申报',
            roles: ['STUDENT'],
            permissionCode: 'student:application:view',
            showInMenu: true,
            menuOrder: 10,
          },
        },
        {
          path: 'student/applications/create',
          name: 'student-application-create',
          component: () => import('@/views/student/ApplicationForm.vue'),
          meta: {
            title: '新增申报',
            roles: ['STUDENT'],
            permissionCode: 'student:application:create',
            showInMenu: true,
            menuOrder: 11,
          },
        },
        {
          path: 'student/applications/:id',
          name: 'student-application-detail',
          component: () => import('@/views/student/ApplicationDetail.vue'),
          meta: {
            title: '申报详情',
            roles: ['STUDENT', 'AUDITOR', 'REVIEWER', 'ADMIN'],
            hidden: true,
          },
        },
        {
          path: 'student/score',
          name: 'student-score',
          component: () => import('@/views/student/MyScore.vue'),
          meta: {
            title: '我的成绩',
            roles: ['STUDENT'],
            permissionCode: 'student:score:view',
            showInMenu: true,
            menuOrder: 12,
          },
        },
        {
          path: 'audit/pending',
          name: 'audit-pending',
          component: () => import('@/views/audit/PendingApplications.vue'),
          meta: {
            title: '待审核列表',
            roles: ['AUDITOR', 'REVIEWER', 'ADMIN'],
            permissionCode: 'audit:pending:view',
            showInMenu: true,
            menuOrder: 20,
          },
        },
        {
          path: 'admin/scores',
          name: 'admin-scores',
          component: () => import('@/views/admin/ScoreRecalculate.vue'),
          meta: {
            title: '成绩重算',
            roles: ['ADMIN'],
            permissionCode: 'admin:score:recalculate',
            showInMenu: true,
            menuOrder: 30,
          },
        },
        {
          path: 'admin/evaluation-categories',
          name: 'admin-evaluation-categories',
          component: () => import('@/views/admin/EvaluationCategoryManage.vue'),
          meta: {
            title: '综测分类管理',
            roles: ['ADMIN'],
            permissionCode: 'admin:evaluation-category:view',
            showInMenu: true,
            menuOrder: 31,
          },
        },
        {
          path: 'admin/evaluation-items',
          name: 'admin-evaluation-items',
          component: () => import('@/views/admin/EvaluationItemManage.vue'),
          meta: {
            title: '综测项目管理',
            roles: ['ADMIN'],
            permissionCode: 'admin:evaluation-item:view',
            showInMenu: true,
            menuOrder: 32,
          },
        },
        {
          path: 'admin/majors',
          name: 'admin-majors',
          component: () => import('@/views/admin/MajorManage.vue'),
          meta: {
            title: '专业管理',
            roles: ['ADMIN'],
            permissionCode: 'admin:major:view',
            showInMenu: true,
            menuOrder: 33,
          },
        },
        {
          path: 'admin/classes',
          name: 'admin-classes',
          component: () => import('@/views/admin/ClassManage.vue'),
          meta: {
            title: '班级管理',
            roles: ['ADMIN'],
            permissionCode: 'admin:class:view',
            showInMenu: true,
            menuOrder: 34,
          },
        },
        {
          path: 'admin/students',
          name: 'admin-students',
          component: () => import('@/views/admin/StudentManage.vue'),
          meta: {
            title: '学生管理',
            roles: ['ADMIN'],
            permissionCode: 'admin:student:view',
            showInMenu: true,
            menuOrder: 35,
          },
        },
        {
          path: 'admin/permissions',
          name: 'admin-permissions',
          component: () => import('@/views/admin/PermissionManage.vue'),
          meta: {
            title: '权限管理',
            roles: ['ADMIN'],
            permissionCode: 'admin:permission:view',
            showInMenu: true,
            menuOrder: 36,
          },
        },
        {
          path: 'admin/role-permissions',
          name: 'admin-role-permissions',
          component: () => import('@/views/admin/RolePermissionManage.vue'),
          meta: {
            title: '角色授权',
            roles: ['ADMIN'],
            permissionCode: 'admin:role-permission:assign',
            showInMenu: true,
            menuOrder: 37,
          },
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  userStore.initFromStorage()

  if (to.path === '/login') {
    if (userStore.token) {
      return getDefaultPath(userStore.roleCode)
    }
    return true
  }

  if (!to.matched.some((record) => record.meta.requiresAuth)) {
    return true
  }

  if (!userStore.token) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (!userStore.sessionChecked) {
    try {
      const currentUser = await getCurrentUser()
      userStore.setCurrentUser(currentUser)
      await userStore.refreshPermissions()
    } catch {
      userStore.clearLogin()
      return {
        path: '/login',
        query: { redirect: to.fullPath },
      }
    }
  }

  if (!canAccessRouteLocation(to, userStore)) {
    ElMessage.warning('当前账号无权访问该页面')
    return getDefaultPath(userStore.roleCode)
  }

  return true
})

export default router
