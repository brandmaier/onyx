## Onyx 1.0-1044 2025-07-30

- deactivated overspecification warning because of unresolved bug
- fixed missing tutorial files

## Onyx 1.0-1043 2025-05-27

- fixed CSV parsing bug with quoted separators (thanks to Tim for spotting); quoted separators are now read properly; AB
- fixed weird long-standing issue with repainting of model views where the outer frame appeared to flicker when model view was moved; problem was due to a faulty clipping command; AB
- fixed two issues with lavaan export regarding regression between latents and formative factor models; contributed by Julian Karch
- fixed issue with faulty descriptive statistics in tool tips of data view when missing values are present
- unicode greek letters are now converted to words when exporting Onyx models to lavaan or Mplus
- fixed code w.r.t. to unsafe strings when converting special characters