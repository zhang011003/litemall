import request from '@/utils/request'

export function dispatchGoods(data) {
  return request({
    url: '/agent/dispatch/',
    method: 'post',
    data
  })
}
