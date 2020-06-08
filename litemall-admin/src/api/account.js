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

export function profitList() {
  return request({
    url: '/profit',
    method: 'get'
  })
}

export function profitHistoryList(query) {
  return request({
    url: '/profithistory/list',
    method: 'get',
    params: query
  })
}

export function cashOut(data) {
  return request({
    url: '/profit/cashout',
    method: 'post',
    data
  })
}
