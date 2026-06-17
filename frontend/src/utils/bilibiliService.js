/**
 * 哔哩哔哩课程视频检索服务
 * 静态课程库 + 动态B站搜索API
 */

// 精选课程视频库（真实B站搜索链接）
const COURSE_VIDEO_LIBRARY = {
  '高等数学': [
    {
      title: '宋浩老师-《高等数学》高清全集',
      author: '宋浩老师官方',
      views: '128万播放',
      query: '宋浩 高等数学 全集',
    },
    {
      title: '同济版《高等数学》课后习题逐题精讲',
      author: '蜂考',
      views: '45万播放',
      query: '高等数学 课后习题 蜂考',
    },
    {
      title: '张宇考研数学-高等数学基础班',
      author: '张宇考研数学',
      views: '210万播放',
      query: '张宇 高等数学 考研',
    },
  ],
  '线性代数': [
    {
      title: '宋浩老师-《线性代数》高清全集',
      author: '宋浩老师官方',
      views: '96万播放',
      query: '宋浩 线性代数 全集',
    },
    {
      title: '3Blue1Brown-线性代数的本质',
      author: '3Blue1Brown',
      views: '89万播放',
      query: '3Blue1Brown 线性代数的本质',
    },
  ],
  '数据结构': [
    {
      title: '王卓老师-《数据结构与算法》通俗易懂',
      author: '青岛大学-王卓',
      views: '96万播放',
      query: '王卓 数据结构 算法',
    },
    {
      title: '郝斌老师-数据结构入门',
      author: '郝斌老师',
      views: '52万播放',
      query: '郝斌 数据结构',
    },
  ],
  '计算机网络': [
    {
      title: '中科大郑烇老师-《计算机网络》',
      author: '中科大郑烇老师',
      views: '68万播放',
      query: '郑烇 计算机网络 中科大',
    },
  ],
  '操作系统': [
    {
      title: '王道考研-操作系统',
      author: '王道论坛',
      views: '73万播放',
      query: '王道考研 操作系统',
    },
  ],
  '数据库': [
    {
      title: 'MySQL数据库教程天花板',
      author: '动力节点',
      views: '55万播放',
      query: 'MySQL数据库教程 动力节点',
    },
  ],
  '概率论': [
    {
      title: '宋浩老师-《概率论与数理统计》',
      author: '宋浩老师官方',
      views: '82万播放',
      query: '宋浩 概率论 数理统计',
    },
  ],
  '大学英语': [
    {
      title: '瑞思拜-大学英语四级全程班',
      author: '我是瑞思拜',
      views: '210万播放',
      query: '瑞思拜 大学英语四级',
    },
  ],
  '计算机组成原理': [
    {
      title: '王道考研-计算机组成原理',
      author: '王道论坛',
      views: '61万播放',
      query: '王道考研 计算机组成原理',
    },
  ],
  'Python': [
    {
      title: '黑马程序员Python入门教程',
      author: '黑马程序员',
      views: '320万播放',
      query: '黑马程序员 Python 入门',
    },
  ],
  'C语言': [
    {
      title: '浙江大学翁恺-C语言程序设计',
      author: '翁恺老师',
      views: '185万播放',
      query: '翁恺 C语言 浙江大学',
    },
  ],
  'Java': [
    {
      title: '尚硅谷Java零基础入门教程',
      author: '尚硅谷',
      views: '450万播放',
      query: '尚硅谷 Java 零基础',
    },
  ],
  '机械设计': [
    {
      title: '西北工大-《机械设计》精品课',
      author: '西北工业大学',
      views: '28万播放',
      query: '机械设计 精品课 视频教程',
    },
  ],
  '土木工程': [
    {
      title: '同济大学-《结构力学》',
      author: '同济大学',
      views: '35万播放',
      query: '结构力学 同济 视频教程',
    },
    {
      title: '重庆大学-《材料力学》',
      author: '重庆大学',
      views: '22万播放',
      query: '材料力学 重庆大学 视频教程',
    },
  ],
};

/** 生成B站搜索链接 */
function searchUrl(query) {
  return `https://search.bilibili.com/all?keyword=${encodeURIComponent(query)}`;
}

/** 模糊匹配课程名 → 返回视频列表 */
function _matchCourse(query) {
  const q = query.toLowerCase();
  for (const [course, videos] of Object.entries(COURSE_VIDEO_LIBRARY)) {
    if (q.includes(course.toLowerCase())) {
      return { course, videos };
    }
  }
  for (const [course, videos] of Object.entries(COURSE_VIDEO_LIBRARY)) {
    const keywords = course.toLowerCase().split(/[、，/ ]/).filter(Boolean);
    if (keywords.some(kw => q.includes(kw))) {
      return { course, videos };
    }
  }
  return null;
}

/**
 * 同步获取精选课程视频（无需网络，即时返回）
 * @param {string} query 搜索关键词
 * @returns {Array} 视频列表
 */
export function getCourseVideos(query) {
  const matched = _matchCourse(query);
  if (matched) {
    return matched.videos.map(v => ({
      ...v,
      url: searchUrl(v.query),
    }));
  }
  return [];
}

/**
 * 搜索B站视频（动态API，有CORS限制时回退）
 * @param {string} query 搜索关键词
 * @returns {Promise<Array>} 视频列表
 */
export async function searchBilibili(query) {
  const matched = _matchCourse(query);
  if (matched) {
    return matched.videos.map(v => ({
      ...v,
      url: searchUrl(v.query),
    }));
  }

  try {
    const keyword = encodeURIComponent(query);
    const resp = await fetch(
      `https://api.bilibili.com/x/web-interface/search/all/v2?keyword=${keyword}&type=video&page_size=3`,
      { signal: AbortSignal.timeout(5000) }
    );
    if (!resp.ok) throw new Error('B站API不可用');
    const json = await resp.json();
    if (json.code === 0 && json.data?.result) {
      const videos = (json.data.result.find(r => r.result_type === 'video')?.data || [])
        .slice(0, 3);
      return videos.map(v => ({
        title: v.title?.replace(/<[^>]+>/g, '') || '',
        author: v.author || '',
        views: _formatViews(v.play || 0),
        url: v.arcurl || searchUrl(query),
        query,
      }));
    }
  } catch {
    // CORS被阻止，回退到搜索页
  }

  return [{
    title: `在B站搜索: ${query}`,
    author: 'B站搜索',
    views: '',
    url: searchUrl(query),
    query,
  }];
}

/** 生成B站搜索链接（兜底方案） */
export function bilibiliSearchUrl(query) {
  return searchUrl(query);
}

function _formatViews(play) {
  if (play >= 10000) return (play / 10000).toFixed(0) + '万播放';
  if (play >= 1000) return (play / 1000).toFixed(1) + '千播放';
  return play + '播放';
}

/**
 * 检测用户问题是否涉及课程学习咨询
 * @param {string} question 用户问题
 * @returns {string|null} 匹配到的课程名
 */
export function detectCourseIntent(question) {
  const learningKeywords = [
    '怎么学', '如何学', '怎么复习', '如何复习', '推荐视频',
    '推荐教程', '教程推荐', '学不会', '看不懂', '视频教程',
    '复习资料', '学习方法', '怎么考', '怎么过',
  ];
  const hasIntent = learningKeywords.some(kw => question.includes(kw));
  if (!hasIntent) return null;

  const matched = _matchCourse(question);
  return matched ? matched.course : null;
}
