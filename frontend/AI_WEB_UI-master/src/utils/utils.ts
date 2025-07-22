import { ElLoading } from 'element-plus'


import CryptoJS from 'crypto-js'
// import CryptoJS from 'crypto-js';  //引用AES源码js
const key = CryptoJS.enc.Utf8.parse("Sp5biS+gX+#CqAFF");  //十六位十六进制数作为密钥
const iv = CryptoJS.enc.Utf8.parse('ud2E8l6wchltwIDA');   //十六位十六进制数作为密钥偏移量

//解密方法
const Decrypt = function (word: any) {
  let encryptedHexStr = CryptoJS.enc.Hex.parse(word);
  let srcs = CryptoJS.enc.Base64.stringify(encryptedHexStr);
  let decrypt = CryptoJS.AES.decrypt(srcs, key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 });
  let decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
  return decryptedStr.toString();
}

// //加密方法
const Encrypt = function (word: any) {
  let srcs = CryptoJS.enc.Utf8.parse(word);
  let encrypted = CryptoJS.AES.encrypt(srcs, key, { iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 });
  return encrypted.ciphertext.toString().toUpperCase();
}

const splitCheckItems = function (str: any) {
  return str.split('；')
}



// 图片预加载
const preloader = function (imgList: any, next: any) {
  let count = 0
  let loadingInstance1 = ElLoading.service({ fullscreen: true, text: '0%' })
  for (let img of imgList) {
    let image = new Image()
    image.src = img.imgUrl
    image.onload = () => {
      count++
      // let point = (count / imgList.length).toFixed(2) * 100
      let point = parseFloat((count / imgList.length).toFixed(2)) * 100
      loadingInstance1.setText(Math.floor(point) + '%')
      if (count == imgList.length) {
        loadingInstance1.setText('100%')
        loadingInstance1.close()
        next()
      }
    }
  }
}

const preloader2 = function (imgList: any, next: any) {
  let count = 0
  for (let img of imgList) {
    let image = new Image()
    image.src = img.imgUrl
    image.onload = () => {
      count++
      if (count == imgList.length) {
        next()
      }
    }
  }
}

// sessionStorage 加解密处理
let oSessionStorage = {
  setItem(key: any, value: any) {
    sessionStorage.setItem(Encrypt(key), Encrypt(value))
  },
  getItem(key: any) {
    if (sessionStorage.getItem(Encrypt(key))) {
      return Decrypt(sessionStorage.getItem(Encrypt(key)))
    } else {
      return null
    }
  },
  removeItem(key: any) {
    sessionStorage.removeItem(Encrypt(key))
  }
}

// 从测试记录附件名中截取用户昵称
let getPetName = function (str: any) {
  let sd = str.split('/')
  let sp = sd[sd.length - 1]

  let a = sp.split('-')
  return a[0]
}

// 载入图片
const requireImg = (imgUrl: string) => {
  return new URL(imgUrl, import.meta.url).href
}

const formatDateRange = (dateString: any) => {
  const date: any = new Date(dateString);
  const today: any = new Date();
  const timeDiff = today - date;
  const daysDiff = Math.floor(timeDiff / (1000 * 60 * 60 * 24));
  if (daysDiff === 0||daysDiff<0) {
    return "今天";
  } else if (daysDiff == 1) {
    return "昨天";
  } else if (daysDiff <= 7) {
    return "一周";
  } else if (daysDiff <= 30) {
    return "30天前";
  } else if (daysDiff <= 60) {
    return "60天";
  } else {
    return "更早";
  }
};


export {
  splitCheckItems,
  preloader,
  preloader2,
  oSessionStorage,
  getPetName,
  requireImg,
  formatDateRange
}
