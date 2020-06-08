import request from '@/utils/request'

export function dispatchGoods(data) {
  return request({
    url: '/agent/dispatch/',
    method: 'post',
    data
  })
}

export function dispatchHistory(query) {
  return request({
    url: '/agent/dispatchHistory/list',
    method: 'get',
    params: query
  })
}
