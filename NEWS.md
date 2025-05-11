## Onyx 1.0-1042 2025-05-XX

- fixed CSV parsing bug with quoted separators (thanks to Tim for spotting); quoted separators are now read properly; AB
- fixed weird long-standing issue with repainting of model views where the outer frame appeared to flicker when model view was moved; problem was due to a faulty clipping command; AB
- fixed two issues with lavaan export regarding regression between latents and formative factor models; contributed by Julian Karch
- fixed issue with faulty descriptive statistics in tool tips of data view when missing values are present