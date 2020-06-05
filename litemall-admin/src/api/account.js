import request from '@/utils/request'

export function accountList() {
  return request({
    url: '/account',
    method: 'get'
  })
}

export function transfer(data) {
  return request({
    url: '/account/transfer',
    method: 'post',
    data
  })
}

export function accountHistoryList(query) {
  return request({
    url: '/accounthistory/list',
    method: 'get',
    params: query
  })
}
