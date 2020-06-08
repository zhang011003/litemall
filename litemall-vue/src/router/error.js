export default [
  {
    path: '/error/:errorType',
    name: 'error',
    component: () => import('@/views/error/error'),
    props: true,
    meta: {
      errorPage : true
    }
  }
];
