import re
from typing import List
from app.models.contract_model import KeyInfoItem

class KeyInfoExtractor:
    def __init__(self):
        self.patterns = {
            '合同金额': [
                r'合同金额[：:]?\s*([\d,\.]+)\s*([元万元亿万元])',
                r'金额[：:]?\s*([\d,\.]+)\s*([元万元亿万元])',
                r'人民币[：:]?\s*([\d,\.]+)\s*([元万元亿万元])',
            ],
            '甲方': [
                r'甲方[（(]\s*([^）)]?\s*[：:]?\s*([^\n，。；]+)',
                r'甲方[：:]?\s*([^\n，。；]+)',
                r'委托方[：:]?\s*([^\n，。；]+)',
            ],
            '乙方': [
                r'乙方[（(]\s*[^）)]*[）)]?\s*[：:]?\s*([^\n，。；]+)',
                r'乙方[：:]?\s*([^\n，。；]+)',
                r'受托方[：:]?\s*([^\n，。；]+)',
            ],
            '合同期限': [
                r'合同期限[：:]?\s*([^\n，。；]+)',
                r'有效期[：:]?\s*([^\n，。；]+)',
                r'期限[：:]?\s*([^\n，。；]+)',
            ],
            '开始日期': [
                r'自[：:]?\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?[起开始]',
                r'开始日期[：:]?\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?',
            ],
            '结束日期': [
                r'至[：:]?\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?[止结束]',
                r'结束日期[：:]?\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?',
            ],
            '签订日期': [
                r'签订日期[：:]?\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?)',
                r'本合同于[：:]?\s*(\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?',
            ],
            '付款方式': [
                r'付款方式[：:]?\s*([^\n。；]+)',
                r'支付方式[：:]?\s*([^\n。；]+)',
            ],
            '违约金': [
                r'违约金[：:]?\s*([^\n。；]+)',
                r'违约条款[：:]?\s*([^\n。；]+)',
            ],
            '争议解决': [
                r'争议解决[：:]?\s*([^\n。；]+)',
                r'解决争议[：:]?\s*([^\n。；]+)',
            ],
            '管辖法院': [
                r'管辖法院[：:]?\s*([^\n。；]+)',
                r'由[^诉?([^法院]+法院)',
            ],
            '违约责任': [
                r'违约责任[：:]?\s*([^\n。；]+)',
            ],
            '保密条款': [
                r'保密条款[：:]?\s*([^\n。；]+)',
                r'保密义务[：:]?\s*([^\n。；]+)',
            ],
        }

    def extract(self, content: str) -> List[KeyInfoItem]:
        results = []
        
        for label, patterns in self.patterns.items():
            value = self._match_pattern(content, patterns)
            if value:
                results.append(KeyInfoItem(
                    label=label,
                    value=value,
                    confidence=0.9
                ))
        
        results = self._extract_amounts(content, results)
        results = self._extract_dates(content, results)
        results = self._extract_parties(content, results)
        
        return results

    def _match_pattern(self, content: str, patterns: list) -> str:
        for pattern in patterns:
            match = re.search(pattern, content)
            if match:
                if len(match.groups()) > 1:
                    return ''.join(match.groups())
                return match.group(1)
        return None

    def _extract_amounts(self, content: str, results: List[KeyInfoItem]) -> List[KeyInfoItem]:
        amount_pattern = None
        
        currency_patterns = [
            r'([\d,\.]+)\s*([元万元亿万元])',
            r'人民币\s*([\d,\.]+)\s*([元万元亿万元])',
        ]
        
        for pattern in currency_patterns:
            matches = re.findall(pattern, content)
            if matches:
                amounts = []
                for match in matches:
                    if isinstance(match, tuple):
                        amounts.append(''.join(match))
                    else:
                        amounts.append(match)
                if amounts:
                    amount_pattern = amounts[0]
                    break
        
        if amount_pattern:
            existing = [r for r in results if r.label == '合同金额']
            if not existing:
                results.append(KeyInfoItem(
                    label='合同金额',
                    value=amount_pattern,
                    confidence=0.85
                ))
        
        return results

    def _extract_dates(self, content: str, results: List[KeyInfoItem]) -> List[KeyInfoItem]:
        date_pattern = r'\d{4}[-/年]\d{1,2}[-/月]\d{1,2}日?'
        dates = re.findall(date_pattern, content)
        
        if dates:
            existing_start = [r for r in results if r.label == '开始日期']
            existing_end = [r for r in results if r.label == '结束日期']
            
            if not existing_start and len(dates) >= 1:
                results.append(KeyInfoItem(
                    label='开始日期',
                    value=dates[0],
                    confidence=0.8
                ))
            if not existing_end and len(dates) >= 2:
                results.append(KeyInfoItem(
                    label='结束日期',
                    value=dates[-1],
                    confidence=0.8
                ))
        
        return results

    def _extract_parties(self, content: str, results: List[KeyInfoItem]) -> List[KeyInfoItem]:
        party_a_patterns = [
            r'甲方[：:]\s*([^\n，。；\s]+(?:公司|集团|有限责任|股份|有限|厂|所|中心))',
        ]
        
        existing_a = [r for r in results if r.label == '甲方']
        existing_b = [r for r in results if r.label == '乙方']
        
        if not existing_a:
            for pattern in party_a_patterns:
                matches = re.findall(pattern, content)
                if matches:
                    results.append(KeyInfoItem(
                        label='甲方',
                        value=matches[0],
                        confidence=0.85
                    ))
                    break
        
        return results
