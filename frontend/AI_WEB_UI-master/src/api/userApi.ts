import { http } from "@/utils/http";
import { pa } from "element-plus/es/locales.mjs";

class UserApi<T> {
  constructor() {
    this.baseUrl = "/user";
  }
  baseUrl: string;
  updateUserName(val: any) {
    return http<any>({
      method: "post",
      url: `${this.baseUrl}/updateUserName`,
      data: { ...val },
    });
  }
  updatePassword(val: any) {
    return http<any>({
      method: "post",
      url: `${this.baseUrl}/updatePassword`,
      data: { ...val },
    });
  }
  updateAvatar(val: any) {
    return http<any>({
      method: "post",
      url: `${this.baseUrl}/updateAvatar`,
      params: { id: val.id },
      data: val.formData,
    });
  }
  updateEmail(val: any) {
    return http<any>({
      method: "post",
      url: `${this.baseUrl}/updateEmail`,
      data: { ...val },
    });
  }
}

export default new UserApi();
