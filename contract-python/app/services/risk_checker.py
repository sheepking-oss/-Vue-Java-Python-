import re
from typing import List
from app.models.contract_model import RiskItem

class RiskChecker:
    def __init__(self):
        self.risk_rules = [
            {
                'category': '违约金风险',
                'level': 'high',
                'check_func': self._check_penalty,
                'message': '违约金比例过高，可能超过法定上限',
                'suggestion': '建议将违约金比例调整至合理范围，通常每日违约金一般不超过0.05%'
            },
            {
                'category': '解除条款风险',
                'level': 'high',
                'check_func': self._check_termination,
                'message': '未明确约定合同解除的具体条件和程序',
                'suggestion': '建议明确约定双方的解除条件、通知方式和异议期限'
            },
            {
                'category': '保密条款风险',
                'level': 'medium',
                'check_func': self._check_confidentiality,
                'message': '缺少保密条款，敏感信息存在泄露风险',
                'suggestion': '建议增加保密条款，明确保密范围、期限和责任'
            },
            {
                'category': '争议解决风险',
                'level': 'medium',
                'check_func': self._check_dispute,
                'message': '争议解决条款不明确或未约定管辖机构',
                'suggestion': '建议明确约定仲裁机构或管辖法院的具体名称'
            },
            {
                'category': '生效条件风险',
                'level': 'medium',
                'check_func': self._check_effective,
                'message': '未明确约定合同生效条件',
                'suggestion': '建议明确约定合同生效的条件和时间'
            },
            {
                'category': '付款条款风险',
                'level': 'medium',
                'check_func': self._check_payment,
                'message': '付款条款不够明确，存在付款风险',
                'suggestion': '建议明确约定付款时间、方式、条件和逾期责任'
            },
            {
                'category': '期限风险',
                'level': 'low',
                'check_func': self._check_duration,
                'message': '合同期限不明确或存在歧义',
                'suggestion': '建议明确约定合同起止日期'
            },
            {
                'category': '违约责任风险',
                'level': 'medium',
                'check_func': self._check_liability,
                'message': '违约责任条款不完整',
                'suggestion': '建议明确约定双方的违约责任和赔偿范围'
            },
        ]

        self.high_risk_keywords = [
            ('高额违约金', '巨额违约金', '惩罚性违约金',
            '不得解除', '无权解除',
            '不得起诉', '不得诉讼',
            '终身保密', '永久保密',
            '全部责任', '全部损失',
        ]

        self.medium_risk_keywords = [
            '争议解决', '管辖法院', '仲裁',
            '保密', '保密义务',
            '解除', '终止',
            '生效', '生效条件',
            '付款', '支付',
            '期限', '有效期',
            '违约责任', '违约条款',
        ]

    def check(self, content: str) -> List[RiskItem]:
        risks = []

        for rule in self.risk_rules:
            if rule['check_func'](content):
                risks.append(RiskItem(
                    level=rule['level'],
                    category=rule['category'],
                    message=rule['message'],
                    suggestion=rule['suggestion']
                ))

        keyword_risks = self._check_keywords(content)
        risks.extend(keyword_risks)

        return risks

    def _check_penalty(self, content: str) -> bool:
        penalty_patterns = [
            r'违约金[：:]?\s*[^。；。\d]*?(?:日|每日|每天)[^。；。\d]*?([\d\.]+)\s*[%‰]',
            r'逾期[^。；。\d]*?([\d\.]+)\s*[%‰][^。；。\d]*?(?:日|每日|每天)',
        ]

        for pattern in penalty_patterns:
            matches = re.findall(pattern, content)
            for match in matches:
                try:
                    rate = float(match)
                    if rate > 0.05:
                        return True
                except ValueError:
                    continue

        high_penalty_keywords = ['高额违约金', '巨额违约金', '惩罚性违约金']
        for keyword in high_penalty_keywords:
            if keyword in content:
                return True

        return False

    def _check_termination(self, content: str) -> bool:
        termination_keywords = ['解除', '终止', '解除合同', '终止合同']
        has_termination = any(kw in content for kw in termination_keywords)
        
        if not has_termination:
            return True

        condition_keywords = ['条件', '情形', '情况', '提前']
        notice_keywords = ['通知', '告知', '书面']
        period_keywords = ['期限', '期间', '提前.*天']

        has_condition = any(kw in content for kw in condition_keywords)
        has_notice = any(kw in content for kw in notice_keywords)
        has_period = any(kw in content for kw in period_keywords)

        if has_termination and not (has_condition and has_notice):
            return True

        return False

    def _check_confidentiality(self, content: str) -> bool:
        confidentiality_keywords = ['保密', '保密条款', '保密义务', '秘密']
        has_confidentiality = any(kw in content for kw in confidentiality_keywords)

        if not has_confidentiality:
            sensitive_keywords = ['商业秘密', '技术秘密', '客户信息', '经营信息']
            has_sensitive = any(kw in content for kw in sensitive_keywords)
            return has_sensitive

        return False

    def _check_dispute(self, content: str) -> bool:
        dispute_keywords = ['争议解决', '解决争议', '管辖', '仲裁', '诉讼', '起诉']
        has_dispute = any(kw in content for kw in dispute_keywords)

        if not has_dispute:
            return True

        specific_institution_keywords = [
            r'仲裁委员会',
            r'人民法院',
            r'法院',
            r'仲裁委',
        ]

        has_specific = False
        for pattern in specific_institution_keywords:
            if re.search(pattern, content):
                has_specific = True
                break

        if has_dispute and not has_specific:
            return True

        return False

    def _check_effective(self, content: str) -> bool:
        effective_keywords = ['生效', '生效条件', '生效时间', '合同生效']
        has_effective = any(kw in content for kw in effective_keywords)

        if not has_effective:
            return True

        return False

    def _check_payment(self, content: str) -> bool:
        payment_keywords = ['付款', '支付', '付款方式', '支付方式']
        has_payment = any(kw in content for kw in payment_keywords)

        if not has_payment:
            amount_pattern = r'[\d,\.]+[元万元亿万元]'
            has_amount = bool(re.search(amount_pattern, content))
            return has_amount

        time_keywords = ['时间', '日期', '期限', '前', '后']
        condition_keywords = ['条件', '验收', '确认', '合格']
        overdue_keywords = ['逾期', '迟延', '延迟']

        has_time = any(kw in content for kw in time_keywords)
        has_condition = any(kw in content for kw in condition_keywords)
        has_overdue = any(kw in content for kw in overdue_keywords)

        if has_payment and not (has_time and (has_condition or has_overdue):
            return True

        return False

    def _check_duration(self, content: str) -> bool:
        duration_keywords = ['期限', '有效期', '合同期限', '有效期限']
        has_duration = any(kw in content for kw in duration_keywords)

        if not has_duration:
            date_pattern = r'\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?'
            dates = re.findall(date_pattern, content)
            return len(dates) < 2

        return False

    def _check_liability(self, content: str) -> bool:
        liability_keywords = ['违约责任', '违约条款', '违约方', '违约责任']
        has_liability = any(kw in content for kw in liability_keywords)

        if not has_liability:
            return True

        specific_keywords = ['赔偿', '损失', '违约金', '责任']
        has_specific = any(kw in content for kw in specific_keywords)

        if has_liability and not has_specific:
            return True

        return False

    def _check_keywords(self, content: str) -> List[RiskItem]:
        risks = []

        for keyword in self.high_risk_keywords:
            if keyword in content:
                risks.append(RiskItem(
                    level='high',
                    category='关键词风险',
                    message=f'发现高风险关键词：{keyword}',
                    suggestion=f'建议审查包含"{keyword}"的条款是否合理'
                ))

        return risks
