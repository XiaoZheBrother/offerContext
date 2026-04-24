import { useEffect } from 'react';
import { Drawer, Form, Input, Select, DatePicker, Radio, Button, message } from 'antd';
import dayjs from 'dayjs';
import { createAnnouncement, updateAnnouncement, getAdminAnnouncementDetail } from '@/services/admin';
import type { FilterOptionsResponse } from '@/types/announcement';
import type { AnnouncementCreateRequest } from '@/types/announcement';

interface AnnouncementFormProps {
  open: boolean;
  editId: number | null;
  filterOptions?: FilterOptionsResponse;
  onClose: () => void;
  onSuccess: () => void;
}

export default function AnnouncementForm({ open, editId, filterOptions, onClose, onSuccess }: AnnouncementFormProps) {
  const [form] = Form.useForm();

  useEffect(() => {
    if (open && editId) {
      getAdminAnnouncementDetail(editId).then((data) => {
        form.setFieldsValue({
          companyName: data.companyName,
          name: data.name,
          detail: data.detail,
          link: data.link,
          applyLink: data.fromUrl,
          classTypeIds: data.classTypeIds ?? [],
          campusTypeIds: data.campusTypeIds ?? [],
          cityIds: data.cityIds ?? [],
          publishedAt: data.publishedAt ? dayjs(data.publishedAt) : undefined,
          expiredAt: data.expiredAt ? dayjs(data.expiredAt) : undefined,
          onlineStatus: 1,
        });
      });
    } else if (open) {
      form.resetFields();
    }
  }, [open, editId, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload: AnnouncementCreateRequest = {
        companyName: values.companyName,
        name: values.name,
        detail: values.detail,
        link: values.link,
        applyLink: values.applyLink,
        classTypeIds: values.classTypeIds,
        campusTypeIds: values.campusTypeIds,
        cityIds: values.cityIds,
        customCities: values.customCities,
        publishedAt: values.publishedAt?.format('YYYY-MM-DD'),
        expiredAt: values.expiredAt?.format('YYYY-MM-DD'),
        onlineStatus: values.onlineStatus ?? 0,
      };

      if (editId) {
        await updateAnnouncement(editId, payload);
        message.success('更新成功');
      } else {
        await createAnnouncement(payload);
        message.success('创建成功');
      }
      onSuccess();
    } catch {
      // form validation error
    }
  };

  return (
    <Drawer
      title={editId ? '编辑公告' : '新增公告'}
      open={open}
      onClose={onClose}
      width={640}
      extra={
        <Button type="primary" onClick={handleSubmit}>
          提交
        </Button>
      }
    >
      <Form form={form} layout="vertical" initialValues={{ onlineStatus: 0 }}>
        <Form.Item name="companyName" label="公司名称" rules={[{ required: true, message: '请输入公司名称' }]}>
          <Input placeholder="企业全称" />
        </Form.Item>

        <Form.Item name="name" label="公告名称" rules={[{ required: true, message: '请输入公告名称' }]}>
          <Input placeholder="招聘公告标题" />
        </Form.Item>

        <Form.Item name="detail" label="招聘公告">
          <Input.TextArea rows={8} placeholder="支持HTML格式内容" />
        </Form.Item>

        <Form.Item name="link" label="宣发网址">
          <Input placeholder="企业招聘公告原始链接" />
        </Form.Item>

        <Form.Item name="applyLink" label="投递入口" rules={[{ required: true, message: '请输入投递链接或邮箱' }]}>
          <Input placeholder="投递链接或邮箱地址" />
        </Form.Item>

        <Form.Item name="classTypeIds" label="毕业年份" rules={[{ required: true, message: '请选择毕业年份' }]}>
          <Select
            mode="multiple"
            placeholder="选择毕业年份"
            options={filterOptions?.classTypes.map((t) => ({ value: t.id, label: t.name })) ?? []}
          />
        </Form.Item>

        <Form.Item name="campusTypeIds" label="招聘批次" rules={[{ required: true, message: '请选择招聘批次' }]}
          extra="选择多个批次时，系统将自动拆分为多条记录"
        >
          <Select
            mode="multiple"
            placeholder="选择招聘批次"
            options={filterOptions?.campusTypes.map((t) => ({ value: t.id, label: t.name })) ?? []}
          />
        </Form.Item>

        <Form.Item name="cityIds" label="工作城市" rules={[{ required: true, message: '请选择城市' }]}>
          <Select
            mode="multiple"
            placeholder="选择城市"
            options={filterOptions?.cities.map((c) => ({ value: c.id, label: c.name })) ?? []}
          />
        </Form.Item>

        <Form.Item name="customCities" label="自定义城市">
          <Select mode="tags" placeholder="输入城市名称后按回车添加" />
        </Form.Item>

        <Form.Item name="publishedAt" label="网申开始日期" rules={[{ required: true, message: '请选择日期' }]}>
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>

        <Form.Item
          name="expiredAt"
          label="网申截止日期"
          rules={[
            { required: true, message: '请选择日期' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || !getFieldValue('publishedAt') || value.isAfter(getFieldValue('publishedAt'))) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('截止日期不能早于开始日期'));
              },
            }),
          ]}
        >
          <DatePicker style={{ width: '100%' }} format="YYYY-MM-DD" />
        </Form.Item>

        <Form.Item name="onlineStatus" label="信息状态">
          <Radio.Group>
            <Radio value={1}>上线</Radio>
            <Radio value={0}>下线</Radio>
          </Radio.Group>
        </Form.Item>
      </Form>
    </Drawer>
  );
}
