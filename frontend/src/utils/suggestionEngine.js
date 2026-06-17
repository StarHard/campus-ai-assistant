/**
 * AI主动追问引擎
 * 根据对话上下文智能生成追问建议 + 官方链接卡片 + 福工官网搜索
 */

// 福建理工大学官方链接库
const OFFICIAL_LINKS = {
  '教务处': { text: '福建理工大学教务处', url: 'https://jwc.fjut.edu.cn' },
  '图书馆': { text: '福建理工大学图书馆', url: 'https://lib.fjut.edu.cn' },
  '信息中心': { text: '校园网 | 信息中心', url: 'https://cmet.fjut.edu.cn' },
  '招生网': { text: '招生办（录取分数线）', url: 'https://join.fjut.edu.cn' },
  '研究生院': { text: '研究生院', url: 'https://yjs.fjut.edu.cn' },
  '学校官网': { text: '福建理工大学官网', url: 'https://www.fjut.edu.cn' },
};

/**
 * 生成福工官网站内搜索链接（Google site:fjut.edu.cn）
 * 仅搜索fjut.edu.cn域内内容，效果等于官网自带搜索
 * @param {string} keyword 搜索关键词
 * @param {string} label  显示文案
 * @returns {{ text: string, url: string, action: string }}
 */
function fjutSearchLink(keyword, label) {
  const encoded = encodeURIComponent(`site:fjut.edu.cn ${keyword}`);
  return {
    text: label || `官网搜：${keyword}`,
    url: `https://www.google.com/search?q=${encoded}`,
    action: 'link',
  };
}

// 场景规则：{ 关键词, 追问建议, 相关官方链接, 官网搜索词 }
const sceneRules = [
  {
    keywords: ['图书馆', '图书', '借书', '还书', '馆藏', '座位'],
    suggestions: [
      { text: '帮我查一下图书馆座位', action: 'send' },
      { text: '图书馆今天几点关门', action: 'send' },
    ],
    links: ['图书馆'],
  },
  {
    keywords: ['教室', '教学楼', '上课', '空教室', '自习室'],
    suggestions: [
      { text: '查询空教室（今天下午）', action: 'navigate', target: '/classrooms' },
      { text: '我下节课在哪个教室', action: 'send' },
    ],
    links: ['教务处'],
  },
  {
    keywords: ['期末', '考试', '复习', '考试时间', '期末考试', '补考', '重修'],
    suggestions: [
      { text: '帮我推荐这门课的复习视频', action: 'send' },
      { text: '帮我整理考试重点', action: 'send' },
    ],
    links: ['教务处'],
  },
  {
    keywords: ['食堂', '吃饭', '餐厅', '饭菜', '早餐', '午餐', '晚餐'],
    suggestions: [
      { text: '食堂今天有什么好吃的', action: 'send' },
      { text: '食堂营业时间是几点到几点', action: 'send' },
    ],
    links: ['学校官网'],
  },
  {
    keywords: ['校园网', 'WiFi', 'wifi', '网络', '上网', '断网', 'VPN', 'vpn'],
    suggestions: [
      { text: '校园网怎么连接认证', action: 'send' },
      { text: '校园网套餐怎么选', action: 'send' },
    ],
    links: ['信息中心'],
  },
  {
    keywords: ['选课', '学分', '选修', '必修', '培养方案', '课程安排'],
    suggestions: [
      { text: '查看当前可选课程', action: 'navigate', target: '/courses' },
      { text: '这门课的老师怎么样', action: 'send' },
    ],
    links: ['教务处'],
  },
  {
    keywords: ['宿舍', '寝室', '住宿', '热水', '水电'],
    suggestions: [
      { text: '宿舍报修流程是什么', action: 'send' },
      { text: '宿舍用电功率限制多少', action: 'send' },
    ],
    links: ['学校官网'],
  },
  {
    keywords: ['一卡通', '校园卡', '充值', '饭卡'],
    suggestions: [
      { text: '校园卡怎么充值', action: 'send' },
      { text: '校园卡丢了怎么挂失', action: 'send' },
    ],
    links: ['信息中心'],
  },
  {
    keywords: ['快递', '包裹', '取件', '收件'],
    suggestions: [
      { text: '学校快递地址是什么', action: 'send' },
      { text: '快递驿站几点关门', action: 'send' },
    ],
    links: ['学校官网'],
  },
  {
    keywords: ['课表', '课程表', '上课时间', '我的课'],
    suggestions: [
      { text: '查看我的课表', action: 'navigate', target: '/schedule' },
      { text: '明天的课在哪个教室', action: 'send' },
    ],
    links: ['教务处'],
  },
  {
    keywords: ['录取', '分数', '分数线', '招生', '高考', '报考'],
    suggestions: [
      { text: '今年录取分数线是多少', action: 'send' },
      { text: '什么专业比较热门', action: 'send' },
    ],
    links: ['招生网'],
  },
  {
    keywords: ['研究生', '考研', '硕士', '博士'],
    suggestions: [
      { text: '研究生招生简章', action: 'send' },
      { text: '有哪些硕士点', action: 'send' },
    ],
    links: ['研究生院', '招生网'],
  },
  {
    keywords: ['学籍', '成绩', '绩点', 'GPA', 'gpa', '毕业', '学位'],
    suggestions: [
      { text: '绩点怎么算的', action: 'send' },
      { text: '毕业要修多少学分', action: 'send' },
    ],
    links: ['教务处'],
  },
  {
    keywords: ['教务', '系统', '密码', '账号', '登录', '统一认证'],
    suggestions: [
      { text: '教务系统密码忘了怎么办', action: 'send' },
      { text: '怎么重置统一认证密码', action: 'send' },
    ],
    links: ['信息中心', '教务处'],
  },

  // ========== 高考生/招生场景 ==========
  // 问专业详情 → 官网搜索
  {
    keywords: ['怎么样', '好不好', '前景', '就业', '排名', '特色', '优势', '实力', '水平'],
    related: ['专业', '学院', '学科', '工程', '计算机', '土木', '机械', '电气', '建筑', '材料', '环境', '管理', '外语', '法学', '设计', '数学', '物理', '化学'],
    suggestions: [
      { text: '这个专业都学哪些课程', action: 'send' },
      { text: '去年录取分数线多少', action: 'send' },
    ],
    links: ['招生网'],
    // 官网搜索会在 generateSuggestions 中动态生成
  },
  {
    keywords: ['培养方案', '培养计划', '课程设置', '学什么', '学哪些', '课程体系', '教学计划'],
    suggestions: [
      { text: '查看所有专业列表', action: 'send' },
      { text: '专业课有哪些', action: 'send' },
    ],
    links: ['教务处', '招生网'],
    // 官网搜索动态生成
  },
  {
    keywords: ['学费', '收费标准', '奖学金', '助学金', '补助'],
    suggestions: [
      { text: '国家奖学金怎么申请', action: 'send' },
      { text: '住宿费一年多少', action: 'send' },
    ],
    links: ['招生网', '学校官网'],
  },
  {
    keywords: ['校区', '环境', '交通', '位置', '地址'],
    suggestions: [
      { text: '学校有几个校区', action: 'send' },
      { text: '附近有什么交通', action: 'send' },
    ],
    links: ['学校官网'],
  },
];

/** 常见福工专业名（用于提取搜索关键词） */
const FJUT_MAJORS = [
  '计算机科学与技术', '软件工程', '数据科学与大数据技术', '人工智能',
  '土木工程', '建筑学', '城乡规划', '给排水科学与工程',
  '机械设计制造及其自动化', '车辆工程', '智能制造工程',
  '电气工程及其自动化', '自动化', '电子信息工程', '通信工程',
  '材料科学与工程', '高分子材料与工程',
  '环境工程', '化学工程与工艺',
  '工程管理', '工程造价', '房地产开发与管理',
  '工商管理', '会计学', '市场营销', '国际经济与贸易',
  '英语', '翻译', '汉语言文学',
  '法学', '知识产权',
  '视觉传达设计', '环境设计', '数字媒体艺术',
  '信息与计算科学', '应用物理学',
];

/**
 * 从用户问题中提取福工专业名
 * @param {string} text 用户问题
 * @returns {string|null}
 */
function extractMajor(text) {
  for (const major of FJUT_MAJORS) {
    if (text.includes(major)) return major;
  }
  // 模糊匹配
  const shortMap = {
    '计算机': '计算机科学与技术',
    '软工': '软件工程',
    '土木': '土木工程',
    '建筑': '建筑学',
    '机械': '机械设计制造及其自动化',
    '电气': '电气工程及其自动化',
    '电子': '电子信息工程',
    '通信': '通信工程',
    '材料': '材料科学与工程',
    '环境': '环境工程',
    '化工': '化学工程与工艺',
    '会计': '会计学',
    '英语': '英语',
    '法学': '法学',
    '设计': '视觉传达设计',
    '数学': '信息与计算科学',
    '物理': '应用物理学',
  };
  for (const [short, full] of Object.entries(shortMap)) {
    if (text.includes(short)) return full;
  }
  return null;
}

/**
 * 检测是否为招生/专业咨询类问题（高考生、家长视角）
 */
function isAdmissionIntent(question) {
  const intentKeywords = [
    '招生', '录取', '分数线', '报考', '高考', '志愿', '报名',
    '专业', '培养方案', '培养计划', '怎么样', '好不好',
    '学什么', '学哪些', '课程设置', '前景', '就业率',
    '师资', '学费', '宿舍条件', '校区', '交通',
    '排名', '特色专业', '王牌专业', '优势学科',
  ];
  return intentKeywords.some(kw => question.includes(kw));
}

/**
 * 分析对话内容，生成追问建议 + 官方链接 + 福工官网搜索
 * @param {string} userQuestion 用户最后的问题
 * @param {string} aiAnswer AI的回答
 * @returns {{ suggestions: Array, links: Array, fjutSearchLinks: Array }}
 */
export function generateSuggestions(userQuestion, aiAnswer) {
  const combinedText = `${userQuestion} ${aiAnswer || ''}`.toLowerCase();

  // 收集所有匹配场景
  const matchedScenes = sceneRules.filter(rule => {
    const mainMatch = rule.keywords.some(kw => combinedText.includes(kw.toLowerCase()));
    if (mainMatch) return true;
    // 检查 related 关键词（用于跨场景匹配）
    if (rule.related) {
      return rule.related.some(kw => combinedText.includes(kw.toLowerCase()));
    }
    return false;
  });

  if (matchedScenes.length === 0) {
    return {
      suggestions: [
        { text: '帮我查一下课表', action: 'send' },
        { text: '查看空教室', action: 'navigate', target: '/classrooms' },
      ],
      links: [],
      fjutSearchLinks: [],
    };
  }

  // 收集追问建议（去重 + 最多3条）
  const allSuggestions = matchedScenes.flatMap(s => s.suggestions);
  const seen = new Set();
  const suggestions = allSuggestions.filter(s => {
    if (seen.has(s.text)) return false;
    seen.add(s.text);
    return true;
  }).slice(0, 3);

  // 收集官方链接（去重 + 最多2个）
  const allLinkKeys = matchedScenes.flatMap(s => s.links);
  const linkSeen = new Set();
  const links = allLinkKeys
    .filter(key => {
      if (linkSeen.has(key)) return false;
      linkSeen.add(key);
      return true;
    })
    .slice(0, 2)
    .map(key => ({
      text: OFFICIAL_LINKS[key].text,
      url: OFFICIAL_LINKS[key].url,
      action: 'link',
    }));

  // ===== 福工官网深度搜索（类似B站搜索逻辑）=====
  const fjutSearchLinks = [];
  if (isAdmissionIntent(userQuestion)) {
    const major = extractMajor(userQuestion);

    if (major) {
      // 有具体专业 → 精准搜索专业介绍+培养方案
      fjutSearchLinks.push(fjutSearchLink(
        `${major} 培养方案`,
        `官网搜：${major}培养方案`
      ));
      fjutSearchLinks.push(fjutSearchLink(
        `${major} 专业介绍`,
        `官网搜：${major}专业介绍`
      ));
    } else {
      // 无具体专业 → 搜索招生信息
      if (userQuestion.includes('分数') || userQuestion.includes('录取')) {
        fjutSearchLinks.push(fjutSearchLink(
          '录取分数线',
          '官网搜：录取分数线'
        ));
      }
      if (userQuestion.includes('专业') || userQuestion.includes('招生')) {
        fjutSearchLinks.push(fjutSearchLink(
          '招生专业',
          '官网搜：招生专业目录'
        ));
      }
      fjutSearchLinks.push(fjutSearchLink(
        '招生章程',
        '官网搜：招生章程'
      ));
    }

    // 限定最多3个搜索链接
    if (fjutSearchLinks.length > 3) {
      fjutSearchLinks.splice(3);
    }
  }

  return { suggestions, links, fjutSearchLinks };
}
